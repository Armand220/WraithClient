using System.IO;
using Newtonsoft.Json;
using WraithClient.Models;

namespace WraithClient.Services;

public static class SettingsService
{
    private static readonly string ConfigDir = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
        "WraithClient");

    private static readonly string ConfigPath = Path.Combine(ConfigDir, "settings.json");

    public static AppSettings Load()
    {
        try
        {
            Directory.CreateDirectory(ConfigDir);
            if (!File.Exists(ConfigPath))
                return CreateDefaults();

            var json = File.ReadAllText(ConfigPath);
            var settings = JsonConvert.DeserializeObject<AppSettings>(json);
            return settings ?? CreateDefaults();
        }
        catch
        {
            return CreateDefaults();
        }
    }

    public static void Save(AppSettings settings)
    {
        try
        {
            Directory.CreateDirectory(ConfigDir);
            var json = JsonConvert.SerializeObject(settings, Formatting.Indented);
            File.WriteAllText(ConfigPath, json);
        }
        catch { /* best effort */ }
    }

    public static string GetDefaultGameDir() =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".wraith");

    // Per-version game dir so mods from different loaders don't contaminate each other.
    public static string GetVersionGameDir(string mcVersion)
    {
        var base_ = GetDefaultGameDir();
        var slug   = mcVersion.Replace('.', '-').Replace(' ', '-');
        return Path.Combine(base_, slug);
    }

    private static AppSettings CreateDefaults()
    {
        var settings = new AppSettings
        {
            GameDirectory = GetDefaultGameDir()
        };
        Save(settings);
        return settings;
    }
}
