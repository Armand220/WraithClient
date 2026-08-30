using System.Diagnostics;
using System.IO;
using System.Net.Http;
using Newtonsoft.Json.Linq;

namespace WraithClient.Services;

// Installs Forge by running the official installer JAR in GUI mode.
// The user clicks "Install Client" → OK, then the process exits and we write the profile.
public static class ForgeInstaller
{
    private static readonly HttpClient Http = new();

    private static string MinecraftDir =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft");

    public static async Task<string> InstallProfileAsync(string mcVersion, Action<string> log,
        CancellationToken ct = default)
    {
        // 1. Resolve recommended Forge build
        log("[Forge] Fetching Forge promotions...");
        var promoJson = await Http.GetStringAsync(
            "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json", ct);
        var promos = JObject.Parse(promoJson)["promos"] as JObject
            ?? throw new Exception("Could not parse Forge promotions.");

        var forgeVersion =
            promos[$"{mcVersion}-recommended"]?.ToString() ??
            promos[$"{mcVersion}-latest"]?.ToString()
            ?? throw new Exception($"No Forge build found for Minecraft {mcVersion}.");

        log($"[Forge] Using Forge {forgeVersion} for MC {mcVersion}");

        // 2. Download installer JAR
        var (coord, installerName) = await ResolveInstallerCoordAsync(mcVersion, forgeVersion, ct);
        log($"[Forge] Coordinate: {coord}");

        var installerPath = Path.Combine(Path.GetTempPath(), installerName);
        if (!File.Exists(installerPath))
        {
            var url = $"https://maven.minecraftforge.net/net/minecraftforge/forge/{coord}/{installerName}";
            log($"[Forge] Downloading installer ({installerName})...");
            var bytes = await Http.GetByteArrayAsync(url, ct);
            await File.WriteAllBytesAsync(installerPath, bytes, ct);
            log("[Forge] Installer downloaded.");
        }
        else
        {
            log("[Forge] Installer already cached.");
        }

        // 3. Run the Forge installer GUI — user clicks "Install Client" then OK
        var javaExe = FindJava();
        if (javaExe == null)
            throw new Exception("Java not found. Install Java and try again.");

        log("[Forge] Opening Forge installer — click \"Install client\" then OK.");
        log("[Forge] Come back here after the installer finishes.");

        var psi = new ProcessStartInfo(javaExe, $"-jar \"{installerPath}\"")
        {
            WorkingDirectory     = MinecraftDir,
            UseShellExecute      = false,
            CreateNoWindow       = false,
            RedirectStandardOutput = false,
            RedirectStandardError  = false,
        };

        var proc = Process.Start(psi) ?? throw new Exception("Failed to start Forge installer.");
        await proc.WaitForExitAsync(ct);

        if (proc.ExitCode != 0)
            throw new Exception($"Forge installer exited with code {proc.ExitCode}.");

        log("[Forge] Installer finished.");

        // 4. Detect the installed Forge version ID
        var forgeVersionId = DetectInstalledForgeVersion(mcVersion, forgeVersion);
        if (forgeVersionId == null)
            throw new Exception("Forge version not found after install. Did you click 'Install client'?");

        log($"[Forge] Detected version ID: {forgeVersionId}");

        // 5. Patch the version JSON with downloads.client so the modern MC Launcher
        //    recognises the version as installed (old Forge installer omits this section).
        await PatchVersionJsonAsync(mcVersion, forgeVersionId, log, ct);

        // 6. Write launcher profile
        await WriteProfileAsync(mcVersion, forgeVersionId, log, ct);
        return forgeVersionId;
    }

    private static string? DetectInstalledForgeVersion(string mcVersion, string forgeVersion)
    {
        // Try the two known coordinate formats
        var candidates = new[]
        {
            $"{mcVersion}-forge{mcVersion}-{forgeVersion}",
            $"{mcVersion}-forge-{forgeVersion}",
            $"{mcVersion}-forge{forgeVersion}",
        };

        foreach (var id in candidates)
        {
            var dir = Path.Combine(MinecraftDir, "versions", id);
            if (Directory.Exists(dir) && File.Exists(Path.Combine(dir, $"{id}.json")))
                return id;
        }

        // Fallback: scan versions dir for any dir containing both mcVersion and forgeVersion
        var versionsDir = Path.Combine(MinecraftDir, "versions");
        if (!Directory.Exists(versionsDir)) return null;
        foreach (var d in Directory.GetDirectories(versionsDir))
        {
            var name = Path.GetFileName(d);
            if (name.Contains(mcVersion) && name.Contains("forge") &&
                File.Exists(Path.Combine(d, $"{name}.json")))
                return name;
        }

        return null;
    }

    private static string? FindJava()
    {
        // Check common Java locations
        var candidates = new List<string>();

        // JAVA_HOME
        var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrEmpty(javaHome))
            candidates.Add(Path.Combine(javaHome, "bin", "java.exe"));

        // Adoptium JDKs
        var local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        var programs = Path.Combine(local, "Programs");
        if (Directory.Exists(programs))
        {
            foreach (var dir in Directory.GetDirectories(programs, "*jdk*", SearchOption.AllDirectories))
                candidates.Add(Path.Combine(dir, "bin", "java.exe"));
            foreach (var dir in Directory.GetDirectories(programs, "*java*", SearchOption.AllDirectories))
                candidates.Add(Path.Combine(dir, "bin", "java.exe"));
        }

        // Program Files JDKs
        foreach (var pf in new[] {
            Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles),
            Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86),
        })
        {
            foreach (var sub in new[] { "Java", "Eclipse Adoptium", "Microsoft", "Zulu", "OpenJDK" })
            {
                var dir = Path.Combine(pf, sub);
                if (!Directory.Exists(dir)) continue;
                foreach (var jdir in Directory.GetDirectories(dir))
                    candidates.Add(Path.Combine(jdir, "bin", "java.exe"));
            }
        }

        // MC Launcher bundled JREs
        foreach (var runtime in new[] {
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                ".minecraft", "runtime"),
        })
        {
            if (!Directory.Exists(runtime)) continue;
            foreach (var f in Directory.GetFiles(runtime, "java.exe", SearchOption.AllDirectories))
                candidates.Add(f);
        }

        // PATH: try plain "java"
        candidates.Add("java");

        return candidates.FirstOrDefault(c => c == "java" || File.Exists(c));
    }

    private static async Task<(string coord, string fileName)> ResolveInstallerCoordAsync(
        string mcVersion, string forgeVersion, CancellationToken ct)
    {
        var plain      = $"{mcVersion}-{forgeVersion}";
        var withSuffix = $"{mcVersion}-{forgeVersion}-{mcVersion}";

        foreach (var coord in new[] { plain, withSuffix })
        {
            var file = $"forge-{coord}-installer.jar";
            var url  = $"https://maven.minecraftforge.net/net/minecraftforge/forge/{coord}/{file}";
            try
            {
                var resp = await Http.SendAsync(new HttpRequestMessage(HttpMethod.Head, url), ct);
                if (resp.IsSuccessStatusCode) return (coord, file);
            }
            catch { }
        }

        return (plain, $"forge-{plain}-installer.jar");
    }

    private static async Task PatchVersionJsonAsync(string mcVersion, string forgeVersionId,
        Action<string> log, CancellationToken ct)
    {
        var forgePath   = Path.Combine(MinecraftDir, "versions", forgeVersionId, $"{forgeVersionId}.json");
        var vanillaPath = Path.Combine(MinecraftDir, "versions", mcVersion, $"{mcVersion}.json");
        if (!File.Exists(forgePath) || !File.Exists(vanillaPath)) return;
        try
        {
            var forgeJson   = JObject.Parse(await File.ReadAllTextAsync(forgePath,   ct));
            var vanillaJson = JObject.Parse(await File.ReadAllTextAsync(vanillaPath, ct));
            if (forgeJson["downloads"] != null) return; // already patched
            var clientDl = vanillaJson["downloads"]?["client"];
            if (clientDl == null) return;
            forgeJson["downloads"] = new JObject { ["client"] = clientDl.DeepClone() };
            await File.WriteAllTextAsync(forgePath,
                forgeJson.ToString(Newtonsoft.Json.Formatting.Indented), ct);
            log("[Forge] Patched version JSON with downloads.client.");
        }
        catch (Exception ex) { log($"[Forge] Warning: could not patch version JSON: {ex.Message}"); }
    }

    private static async Task WriteProfileAsync(string mcVersion, string forgeVersionId,
        Action<string> log, CancellationToken ct)
    {
        var profilesPath = Path.Combine(MinecraftDir, "launcher_profiles.json");

        JObject root;
        if (File.Exists(profilesPath))
        {
            try { root = JObject.Parse(await File.ReadAllTextAsync(profilesPath, ct)); }
            catch { root = new JObject(); }
        }
        else { root = new JObject(); }

        if (root["profiles"] is not JObject profiles)
        {
            profiles = new JObject();
            root["profiles"] = profiles;
        }

        var wraithGameDir = SettingsService.GetVersionGameDir(mcVersion);
        Directory.CreateDirectory(wraithGameDir);

        var profileKey = $"wraith-forge-{mcVersion}";
        profiles[profileKey] = new JObject
        {
            ["name"]          = $"Wraith {mcVersion}",
            ["type"]          = "custom",
            ["created"]       = DateTime.UtcNow.ToString("O"),
            ["lastUsed"]      = DateTime.UtcNow.ToString("O"),
            ["lastVersionId"] = forgeVersionId,
            ["gameDir"]       = wraithGameDir,
            ["icon"]          = "Furnace"
        };

        await File.WriteAllTextAsync(profilesPath,
            root.ToString(Newtonsoft.Json.Formatting.Indented), ct);

        log($"[Forge] Profile \"Wraith {mcVersion}\" written (game dir: {wraithGameDir}).");
    }
}
