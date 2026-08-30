namespace WraithClient.Models;

public class AppSettings
{
    public string  Username         { get; set; } = "Player";
    public string  SelectedVersion  { get; set; } = "1.8.9";
    public int     MaxRamMb         { get; set; } = 2048;
    public int     MinRamMb         { get; set; } = 512;
    public bool    Fullscreen       { get; set; } = false;
    public int     WindowWidth      { get; set; } = 1280;
    public int     WindowHeight     { get; set; } = 720;
    public string  GameDirectory    { get; set; } = string.Empty;
    public string? JavaPath         { get; set; }
    public string  JvmArgs          { get; set; } = string.Empty;
    public string? QuickConnectIp   { get; set; }
    public int     QuickConnectPort { get; set; } = 25565;
    public bool    DiscordRpc       { get; set; } = true;
    public bool    KeepLauncherOpen { get; set; } = false;
    public bool    InjectWraithMod  { get; set; } = true;
    public AuthMode AuthMode        { get; set; } = AuthMode.Offline;

    // Enabled cheats: key = cheat mod id, value = jar filename to inject
    public Dictionary<string, string> EnabledCheats { get; set; } = new();

    // Moderator customisation (persisted when mod saves)
    public double   LauncherWidth   { get; set; } = 1060;
    public double   LauncherHeight  { get; set; } = 660;
    public double   SidebarWidth    { get; set; } = 72;
    public bool     ShowNavLabels   { get; set; } = false;
}

public enum AuthMode { Offline, Microsoft }
