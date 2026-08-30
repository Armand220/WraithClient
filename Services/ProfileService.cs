using System.IO;
using System.Text.Json;

namespace WraithClient.Services;

public class LaunchProfile
{
    public string Id             { get; set; } = Guid.NewGuid().ToString();
    public string Name           { get; set; } = "Default";
    public string MinecraftVersion { get; set; } = "1.21.4";
    public string ModLoader      { get; set; } = "Fabric";
    public string ModLoaderVersion { get; set; } = "";
    public string GameDirectory  { get; set; } = "";
    public int    MemoryMB       { get; set; } = 2048;
    public bool   IsActive       { get; set; }
}

public static class ProfileService
{
    private static readonly string ProfilesPath =
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                     "WraithClient", "profiles.json");

    public static List<LaunchProfile> Load()
    {
        try
        {
            if (!File.Exists(ProfilesPath)) return [DefaultProfile()];
            var list = JsonSerializer.Deserialize<List<LaunchProfile>>(File.ReadAllText(ProfilesPath));
            return list?.Count > 0 ? list : [DefaultProfile()];
        }
        catch { return [DefaultProfile()]; }
    }

    public static void Save(List<LaunchProfile> profiles)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(ProfilesPath)!);
        File.WriteAllText(ProfilesPath, JsonSerializer.Serialize(profiles, new JsonSerializerOptions { WriteIndented = true }));
    }

    private static LaunchProfile DefaultProfile() => new()
    {
        Name = "Default", MinecraftVersion = "1.21.4", ModLoader = "Fabric",
        MemoryMB = 2048, IsActive = true
    };
}
