using System.IO;
using System.Net.Http;
using Newtonsoft.Json.Linq;

namespace WraithClient.Services;

public static class FabricInstaller
{
    private static readonly HttpClient Http = new();

    private static string MinecraftDir =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft");

    /// <summary>
    /// Downloads the Fabric version JSON for the given MC version, writes it to
    /// .minecraft/versions/, and upserts a "Wraith {version}" profile in
    /// launcher_profiles.json — exactly what the Fabric installer does.
    /// Returns the Fabric version ID on success.
    /// </summary>
    public static async Task<string> InstallProfileAsync(string mcVersion, Action<string> log,
        CancellationToken ct = default)
    {
        // 1. Resolve latest stable Fabric loader for this MC version
        log($"[Fabric] Fetching loader info for {mcVersion}...");
        var loadersJson = await Http.GetStringAsync(
            $"https://meta.fabricmc.net/v2/versions/loader/{mcVersion}", ct);
        var loaders = JArray.Parse(loadersJson);

        if (!loaders.Any())
            throw new Exception(
                $"Fabric Loader is not available for Minecraft {mcVersion}.\n" +
                "Fabric supports 1.14+ (Java Edition). Try a different version.");

        // Pick the first stable loader (they're sorted newest-first)
        var loaderVersion = loaders
            .FirstOrDefault(l => l["loader"]?["stable"]?.Value<bool>() == true)
            ?["loader"]?["version"]?.ToString()
            ?? loaders[0]["loader"]!["version"]!.ToString();

        var fabricId = $"fabric-loader-{loaderVersion}-{mcVersion}";
        log($"[Fabric] Using loader {loaderVersion} → {fabricId}");

        // 2. Download version profile JSON from Fabric meta and save it
        var versionsDir = Path.Combine(MinecraftDir, "versions", fabricId);
        Directory.CreateDirectory(versionsDir);

        var versionJsonPath = Path.Combine(versionsDir, $"{fabricId}.json");
        log("[Fabric] Downloading version JSON...");
        var versionJson = await Http.GetStringAsync(
            $"https://meta.fabricmc.net/v2/versions/loader/{mcVersion}/{loaderVersion}/profile/json", ct);
        await File.WriteAllTextAsync(versionJsonPath, versionJson, ct);

        // 3. Upsert profile in launcher_profiles.json
        log("[Fabric] Writing launcher profile...");
        var profilesPath = Path.Combine(MinecraftDir, "launcher_profiles.json");

        JObject root;
        if (File.Exists(profilesPath))
        {
            try { root = JObject.Parse(await File.ReadAllTextAsync(profilesPath, ct)); }
            catch { root = new JObject(); }
        }
        else
        {
            root = new JObject();
        }

        if (root["profiles"] is not JObject profiles)
        {
            profiles = new JObject();
            root["profiles"] = profiles;
        }

        // Use a dedicated game directory so the Wraith profile is fully isolated
        // from the user's main .minecraft (avoids conflicts with other clients/mods).
        var wraithGameDir = SettingsService.GetVersionGameDir(mcVersion);
        Directory.CreateDirectory(wraithGameDir);

        var profileKey = $"wraith-{mcVersion}";
        profiles[profileKey] = new JObject
        {
            ["name"]          = $"Wraith {mcVersion}",
            ["type"]          = "custom",
            ["created"]       = DateTime.UtcNow.ToString("O"),
            ["lastUsed"]      = DateTime.UtcNow.ToString("O"),
            ["lastVersionId"] = fabricId,
            ["gameDir"]       = wraithGameDir,
            ["icon"]          = "Crafting_Table"
        };

        await File.WriteAllTextAsync(profilesPath,
            root.ToString(Newtonsoft.Json.Formatting.Indented), ct);

        log($"[Fabric] Profile \"Wraith {mcVersion}\" ready (game dir: {wraithGameDir}).");

        // 4. Download Fabric API into the mods dir if not already present
        var modsDir = Path.Combine(wraithGameDir, "mods");
        await DownloadFabricApiAsync(mcVersion, modsDir, log, ct);

        return fabricId;
    }

    /// <summary>
    /// Fetches the latest Fabric API release for the given MC version from Modrinth
    /// and downloads it into modsDir if it isn't already there.
    /// </summary>
    private static async Task DownloadFabricApiAsync(string mcVersion, string modsDir,
        Action<string> log, CancellationToken ct)
    {
        try
        {
            log("[Fabric] Fetching Fabric API version...");

            // Modrinth project ID for Fabric API is "P7dR8mSH"
            var url = "https://api.modrinth.com/v2/project/P7dR8mSH/version" +
                      $"?game_versions=%5B%22{Uri.EscapeDataString(mcVersion)}%22%5D" +
                      "&loaders=%5B%22fabric%22%5D";

            var resp = await Http.GetStringAsync(url, ct);
            var versions = JArray.Parse(resp);

            if (!versions.Any())
            {
                log($"[Fabric] No Fabric API release found for {mcVersion} — skipping.");
                return;
            }

            // Primary file of the latest release
            var primaryFile = versions[0]["files"]?
                .FirstOrDefault(f => f["primary"]?.Value<bool>() == true)
                ?? versions[0]["files"]?[0];

            if (primaryFile == null)
            {
                log("[Fabric] Could not resolve Fabric API download URL — skipping.");
                return;
            }

            var fileName    = primaryFile["filename"]!.ToString();
            var downloadUrl = primaryFile["url"]!.ToString();
            var destPath    = Path.Combine(modsDir, fileName);

            if (File.Exists(destPath))
            {
                log($"[Fabric] Fabric API already present: {fileName}");
                return;
            }

            // Remove any older Fabric API jars to avoid duplicates
            Directory.CreateDirectory(modsDir);
            foreach (var old in Directory.GetFiles(modsDir, "fabric-api-*.jar"))
                File.Delete(old);

            log($"[Fabric] Downloading Fabric API {fileName}...");
            var bytes = await Http.GetByteArrayAsync(downloadUrl, ct);
            await File.WriteAllBytesAsync(destPath, bytes, ct);
            log($"[Fabric] Fabric API installed.");
        }
        catch (Exception ex)
        {
            log($"[Fabric] Warning: could not auto-download Fabric API: {ex.Message}");
        }
    }
}
