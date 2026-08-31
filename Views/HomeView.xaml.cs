using System.Diagnostics;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using WraithClient.Models;
using WraithClient.Services;

namespace WraithClient.Views;

public partial class HomeView : Page
{
    private readonly MainWindow _main;
    private readonly VersionManager _versions = new();
    private readonly MinecraftLaunchService _launcher = new();
    private bool _gameRunning;
    private bool _loadingVersions;
    private bool _premiumMode;

    public HomeView(MainWindow main)
    {
        _main = main;
        InitializeComponent();
        _launcher.ProgressChanged += OnProgress;
        _launcher.LogReceived     += OnLog;
        _launcher.GameExited      += OnExited;
        Loaded += async (_, _) =>
        {
            _premiumMode = App.Settings.AuthMode == AuthMode.Microsoft;
            UsernameBox.Text = App.Settings.Username;
            IpBox.Text   = App.Settings.QuickConnectIp ?? "";
            PortBox.Text = App.Settings.QuickConnectPort.ToString();
            ApplyModeUI();
            RefreshInfo();
            await LoadVersionsAsync();
        };
    }

    // ── Tab toggle ────────────────────────────────────────────────────────

    private void CrackedTab_Click(object sender, System.Windows.Input.MouseButtonEventArgs e)
    {
        if (!_premiumMode) return;
        _premiumMode = false;
        App.Settings.AuthMode = AuthMode.Offline;
        SettingsService.Save(App.Settings);
        ApplyModeUI();
        RefreshInfo();
    }

    private void PremiumTab_Click(object sender, System.Windows.Input.MouseButtonEventArgs e)
    {
        if (_premiumMode) return;
        _premiumMode = true;
        App.Settings.AuthMode = AuthMode.Microsoft;
        SettingsService.Save(App.Settings);
        ApplyModeUI();
        RefreshInfo();
    }

    private void ApplyModeUI()
    {
        var accent  = (SolidColorBrush)FindResource("AccentBrush");
        var surface = (SolidColorBrush)FindResource("Surface2Brush");
        var muted   = (SolidColorBrush)FindResource("TextMutedBrush");
        var black   = new SolidColorBrush(Colors.Black);

        CrackedTab.Background      = _premiumMode ? surface : accent;
        CrackedTabLabel.Foreground = _premiumMode ? muted   : black;
        PremiumTab.Background      = _premiumMode ? accent  : surface;
        PremiumTabLabel.Foreground = _premiumMode ? black   : muted;

        UsernameRow.Visibility = _premiumMode ? Visibility.Collapsed : Visibility.Visible;

        AccountLabel.Text = _premiumMode
            ? "Premium — opens Minecraft Launcher"
            : $"Cracked — {App.Settings.Username}";
        AccountDot.Fill = _premiumMode
            ? (SolidColorBrush)FindResource("SuccessBrush")
            : (SolidColorBrush)FindResource("WarningBrush");
    }

    private void RefreshInfo()
    {
        InfoMode.Text = _premiumMode ? "Premium" : "Cracked";
        InfoVer.Text  = App.Settings.SelectedVersion;
        InfoRam.Text  = $"{App.Settings.MaxRamMb} MB";
        InfoMod.Text  = App.Settings.InjectWraithMod
            ? (IsFabricSupported(App.Settings.SelectedVersion) ? "Fabric" : "Forge")
            : "Disabled";
    }

    // Fabric supports 1.14 and above (including 26.x year-based versions).
    // 1.8.x through 1.13.x require Forge instead.
    private static bool IsFabricSupported(string version) =>
        version.StartsWith("26.") ||
        version.StartsWith("1.14") || version.StartsWith("1.15") ||
        version.StartsWith("1.16") || version.StartsWith("1.17") ||
        version.StartsWith("1.18") || version.StartsWith("1.19") ||
        version.StartsWith("1.20") || version.StartsWith("1.21");

    // ── Versions ──────────────────────────────────────────────────────────

    private async Task LoadVersionsAsync(bool snapshots = false)
    {
        if (_loadingVersions) return;
        _loadingVersions = true;
        SetStatus("Loading versions...", false);
        try
        {
            var list = await _versions.GetVersionsAsync(snapshots);
            VersionCombo.ItemsSource       = list;
            VersionCombo.DisplayMemberPath = "DisplayName";
            var sel = list.FirstOrDefault(v => v.Id == App.Settings.SelectedVersion)
                      ?? list.FirstOrDefault();
            VersionCombo.SelectedItem = sel;
            SetStatus("Ready", true);
        }
        catch (Exception ex) { SetStatus($"Error: {ex.Message}", false); }
        finally { _loadingVersions = false; }
    }

    private void VersionCombo_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_loadingVersions || VersionCombo.SelectedItem is not MCVersion v) return;
        App.Settings.SelectedVersion = v.Id;
        InfoVer.Text = v.Id;
        InfoMod.Text = App.Settings.InjectWraithMod
            ? (IsFabricSupported(v.Id) ? "Fabric" : "Forge")
            : "Disabled";
        SettingsService.Save(App.Settings);
        _main.UpdateVersionBadge();
        if (App.Settings.DiscordRpc && !_gameRunning)
            App.Discord.SetIdle();
    }

    private async void Snapshots_Changed(object sender, RoutedEventArgs e)
        => await LoadVersionsAsync(SnapshotsCheck.IsChecked == true);

    private void Connect_Click(object sender, RoutedEventArgs e)
    {
        App.Settings.QuickConnectIp   = string.IsNullOrWhiteSpace(IpBox.Text) ? null : IpBox.Text.Trim();
        App.Settings.QuickConnectPort = int.TryParse(PortBox.Text, out var p) ? p : 25565;
        SettingsService.Save(App.Settings);
    }

    // ── Play ──────────────────────────────────────────────────────────────

    private async void Play_Click(object sender, RoutedEventArgs e)
    {
        if (_gameRunning) return;

        if (_premiumMode)
        {
            LaunchPremium();
            return;
        }

        // ── Cracked ──────────────────────────────────────────────────────
        var name = UsernameBox.Text.Trim();
        if (name.Length < 2) { SetStatus("Enter a username (min 2 chars).", false); return; }
        App.Settings.Username = name;

        App.Settings.QuickConnectIp   = string.IsNullOrWhiteSpace(IpBox.Text) ? null : IpBox.Text.Trim();
        App.Settings.QuickConnectPort = int.TryParse(PortBox.Text, out var p) ? p : 25565;
        SettingsService.Save(App.Settings);

        var version   = App.Settings.SelectedVersion;
        var useFabric = IsFabricSupported(version);
        var needsMods = App.Settings.InjectWraithMod || App.Settings.EnabledCheats.Count > 0;

        _gameRunning = true;
        PlayBtn.IsEnabled         = false;
        PlayBtn.Content           = "Setting up...";
        StopBtn.Visibility        = Visibility.Visible;
        LaunchProgress.Visibility = Visibility.Visible;

        // Install Fabric/Forge when any mod is active so the loader is present
        string? fabricVersionId = null;
        string? modsDir         = null;

        if (needsMods)
        {
            if (useFabric)
            {
                SetStatus("Installing Fabric...", false);
                try
                {
                    fabricVersionId = await FabricInstaller.InstallProfileAsync(version, Log);
                    modsDir = Path.Combine(SettingsService.GetVersionGameDir(version), "mods");
                }
                catch (Exception ex)
                {
                    Log($"[Wraith] Fabric install failed: {ex.Message}");
                    SetStatus("Fabric install failed", false);
                    ResetPlay();
                    return;
                }
            }
            else
            {
                SetStatus($"Installing Forge for {version}...", false);
                try
                {
                    void ThreadSafeLog(string l) => Dispatcher.Invoke(() => Log(l));
                    await ForgeInstaller.InstallProfileAsync(version, ThreadSafeLog);
                    modsDir = GetCrackedModsDir();
                }
                catch (Exception ex)
                {
                    Log($"[Wraith] Forge install failed: {ex.Message}");
                    SetStatus("Forge install failed", false);
                    ResetPlay();
                    return;
                }
            }
        }

        modsDir ??= GetCrackedModsDir();

        if (App.Settings.InjectWraithMod)
            InjectMod(modsDir);

        InjectCheats(modsDir);

        PlayBtn.Content = "Launching...";

        if (App.Settings.DiscordRpc)
            App.Discord.SetPlaying(version, App.Settings.QuickConnectIp);

        Log($"[Wraith] Launching {version} (cracked, {(fabricVersionId != null ? "Fabric" : "vanilla")})...");

        var gameDir = fabricVersionId != null
            ? SettingsService.GetVersionGameDir(version)
            : null;

        try
        {
            await _launcher.LaunchAsync(App.Settings, AuthService.GetOfflineSession(name),
                versionOverride: fabricVersionId,
                gameDirOverride: gameDir);
        }
        catch (Exception ex)
        {
            Log($"[Wraith] Error: {ex.Message}");
            SetStatus("Launch failed", false);
            ResetPlay();
        }
    }

    // ── Premium: install Fabric/Forge profile, inject mod, open launcher ──────────

    private async void LaunchPremium()
    {
        var version    = App.Settings.SelectedVersion;
        var useFabric  = IsFabricSupported(version);
        // ForgeInstaller logs from thread-pool threads (process output events),
        // so wrap Log in a Dispatcher.Invoke to avoid cross-thread crashes.
        void ThreadSafeLog(string line) => Dispatcher.Invoke(() => Log(line));

        var launcherExe = FindMinecraftLauncher();
        if (launcherExe == null)
        {
            MessageBox.Show(
                "Could not find the Minecraft Launcher.\n\n" +
                "Install it from minecraft.net or the Microsoft Store, then try again.",
                "Wraith Client", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        PlayBtn.IsEnabled = false;
        PlayBtn.Content   = "Setting up...";
        LaunchProgress.Visibility = Visibility.Visible;

        if (useFabric)
        {
            SetStatus("Installing Fabric...", false);
            try
            {
                await FabricInstaller.InstallProfileAsync(version, Log);
            }
            catch (Exception ex)
            {
                Log($"[Wraith] Fabric install failed: {ex.Message}");
                SetStatus("Fabric install failed", false);
                PlayBtn.IsEnabled = true;
                PlayBtn.Content   = "PLAY";
                LaunchProgress.Visibility = Visibility.Collapsed;
                return;
            }
        }
        else
        {
            // Forge-only version (1.8.x – 1.13.x) — auto-install Forge
            SetStatus($"Installing Forge for {version}...", false);
            try
            {
                await ForgeInstaller.InstallProfileAsync(version, ThreadSafeLog);
            }
            catch (Exception ex)
            {
                Log($"[Wraith] Forge install failed: {ex.Message}");
                SetStatus("Forge install failed", false);
                PlayBtn.IsEnabled = true;
                PlayBtn.Content   = "PLAY";
                LaunchProgress.Visibility = Visibility.Collapsed;
                return;
            }
        }

        if (App.Settings.InjectWraithMod)
            InjectMod(GetOfficialModsDir());

        InjectCheats(GetOfficialModsDir());

        LaunchProgress.Visibility = Visibility.Collapsed;

        if (useFabric)
            Log("[Wraith] Opening Minecraft Launcher — select the \"Wraith\" profile and play.");
        else
            Log("[Wraith] Opening Minecraft Launcher — select \"Wraith 1.8.9\" and play.");

        if (App.Settings.DiscordRpc)
            App.Discord.SetPlaying(version, App.Settings.QuickConnectIp);

        // Kill any running Minecraft Launcher instances, then relaunch fresh
        await KillMinecraftLauncherAsync(launcherExe);

        try
        {
            Process.Start(new ProcessStartInfo(launcherExe) { UseShellExecute = true });
            SetStatus(useFabric ? "Minecraft Launcher opened (Fabric)" : "Minecraft Launcher opened (Forge)", true);
        }
        catch (Exception ex)
        {
            Log($"[Wraith] Failed to open launcher: {ex.Message}");
            SetStatus("Failed to open launcher", false);
        }

        PlayBtn.IsEnabled = true;
        PlayBtn.Content   = "PLAY";
    }

    // Each MC version gets its own game dir so mods never bleed across loaders/versions.
    private static string GetOfficialModsDir() =>
        Path.Combine(SettingsService.GetVersionGameDir(App.Settings.SelectedVersion), "mods");

    private static string GetCrackedModsDir()
    {
        var gameDir = string.IsNullOrEmpty(App.Settings.GameDirectory)
            ? SettingsService.GetDefaultGameDir()
            : App.Settings.GameDirectory;
        return Path.Combine(gameDir, "mods");
    }

    private async Task KillMinecraftLauncherAsync(string launcherExe)
    {
        bool killed = false;

        // Primary: match by the actual launcher exe path
        foreach (var proc in Process.GetProcesses())
        {
            try
            {
                if (string.Equals(proc.MainModule?.FileName, launcherExe,
                        StringComparison.OrdinalIgnoreCase))
                {
                    Log($"[Wraith] Killing launcher by path: {proc.ProcessName} (PID {proc.Id})");
                    proc.Kill();
                    proc.WaitForExit(3000);
                    killed = true;
                }
            }
            catch { }
        }

        // Fallback: all known launcher process names
        foreach (var name in new[] {
            "MinecraftLauncher", "Minecraft", "minecraft",
            "Minecraft Launcher", "launcher", "LauncherUI",
            "minecraftlauncher"
        })
        {
            foreach (var proc in Process.GetProcessesByName(name))
            {
                try
                {
                    Log($"[Wraith] Killing launcher by name '{name}': PID {proc.Id}");
                    proc.Kill();
                    proc.WaitForExit(3000);
                    killed = true;
                }
                catch { }
            }
        }

        // Last resort: scan all processes for anything with "minecraft" in the name or window title
        if (!killed)
        {
            foreach (var proc in Process.GetProcesses())
            {
                try
                {
                    bool nameMatch = proc.ProcessName.Contains("minecraft", StringComparison.OrdinalIgnoreCase)
                                  || proc.ProcessName.Contains("launcher", StringComparison.OrdinalIgnoreCase);
                    bool titleMatch = !string.IsNullOrEmpty(proc.MainWindowTitle)
                                   && proc.MainWindowTitle.Contains("Minecraft Launcher", StringComparison.OrdinalIgnoreCase);
                    if (nameMatch || titleMatch)
                    {
                        Log($"[Wraith] Killing launcher (scan): {proc.ProcessName} '{proc.MainWindowTitle}' PID {proc.Id}");
                        proc.Kill();
                        proc.WaitForExit(3000);
                        killed = true;
                    }
                }
                catch { }
            }
        }

        if (killed)
        {
            Log("[Wraith] Closed existing Minecraft Launcher.");
            await Task.Delay(900);
        }
        else
        {
            Log("[Wraith] Minecraft Launcher was not running (nothing to close).");
        }
    }

    private static string? FindMinecraftLauncher()
    {
        var pf86  = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86);
        var pf    = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
        var local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);

        var candidates = new[]
        {
            Path.Combine(pf86,  "Minecraft Launcher", "MinecraftLauncher.exe"),
            Path.Combine(pf,    "Minecraft Launcher", "MinecraftLauncher.exe"),
            @"C:\XboxGames\Minecraft Launcher\Content\Minecraft.exe",
            Path.Combine(pf86,  "Minecraft", "MinecraftLauncher.exe"),
            Path.Combine(pf,    "Minecraft", "MinecraftLauncher.exe"),
            Path.Combine(local, "Programs", "Minecraft Launcher", "MinecraftLauncher.exe"),
        };

        return candidates.FirstOrDefault(File.Exists);
    }

    // ── Mod injection ─────────────────────────────────────────────────────

    private void InjectMod(string modsDir)
    {
        try
        {
            var jarName = GetModJarName(App.Settings.SelectedVersion);
            if (jarName == null)
            {
                Log($"[Wraith] No mod build available for {App.Settings.SelectedVersion} yet — skipping.");
                return;
            }
            var modSrc = Path.Combine(AppContext.BaseDirectory, jarName);
            if (!File.Exists(modSrc))
            {
                // Remove any stale WraithMod.jar so the wrong build doesn't load
                var stale = Path.Combine(modsDir, "WraithMod.jar");
                if (File.Exists(stale)) { File.Delete(stale); Log("[Wraith] Removed stale mod jar."); }
                Log($"[Wraith] {jarName} not found in publish/ — build it first with gradlew build in the matching WraithMod project.");
                return;
            }
            Directory.CreateDirectory(modsDir);
            File.Copy(modSrc, Path.Combine(modsDir, "WraithMod.jar"), overwrite: true);
            Log($"[Wraith] Injected {jarName} into {modsDir}");
        }
        catch (Exception ex) { Log($"[Wraith] Mod inject failed: {ex.Message}"); }
    }

    private void InjectCheats(string modsDir)
    {
        try
        {
            var cheatsDir = CheatsView.CheatsDir;
            if (!Directory.Exists(cheatsDir)) return;

            Directory.CreateDirectory(modsDir);

            // Build the full set of known cheat filenames (all jars in cheats/ and deps/)
            var knownCheatFiles = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
            foreach (var f in Directory.EnumerateFiles(cheatsDir, "*.jar"))
                knownCheatFiles.Add(Path.GetFileName(f));
            var depsDir = Path.Combine(cheatsDir, "deps");
            if (Directory.Exists(depsDir))
                foreach (var f in Directory.EnumerateFiles(depsDir, "*.jar"))
                    knownCheatFiles.Add(Path.GetFileName(f));

            // Remove any cheat jars already in the mods folder that are now disabled
            var enabled = App.Settings.EnabledCheats; // id → filename
            var enabledFiles = new HashSet<string>(enabled.Values, StringComparer.OrdinalIgnoreCase);
            foreach (var existing in Directory.EnumerateFiles(modsDir, "*.jar"))
            {
                var name = Path.GetFileName(existing);
                if (knownCheatFiles.Contains(name) && !enabledFiles.Contains(name))
                {
                    File.Delete(existing);
                    Log($"[Wraith] Removed disabled cheat: {name}");
                }
            }

            if (enabled.Count == 0) return;

            // Inject enabled cheat jars
            foreach (var (_, fileName) in enabled)
            {
                var src = Path.Combine(cheatsDir, fileName);
                if (!File.Exists(src)) continue;
                File.Copy(src, Path.Combine(modsDir, fileName), overwrite: true);
                Log($"[Wraith] Injected cheat: {fileName}");
            }

            // Inject shared dependency jars only if at least one cheat is enabled
            if (Directory.Exists(depsDir))
            {
                foreach (var dep in Directory.EnumerateFiles(depsDir, "*.jar"))
                {
                    File.Copy(dep, Path.Combine(modsDir, Path.GetFileName(dep)), overwrite: true);
                    Log($"[Wraith] Injected dep: {Path.GetFileName(dep)}");
                }
            }
        }
        catch (Exception ex) { Log($"[Wraith] Cheat inject failed: {ex.Message}"); }
    }

    private static string? GetModJarName(string version)
    {
        if (!IsFabricSupported(version))
            return (version.StartsWith("1.12") || version.StartsWith("1.8")) ? "WraithMod-Forge.jar" : null;
        if (version.StartsWith("26."))  return "WraithMod-26.jar";
        if (version.StartsWith("1.21")) return "WraithMod-1.21.jar";
        if (version.StartsWith("1.20")) return "WraithMod-1.20.jar";
        if (version.StartsWith("1.19") || version.StartsWith("1.18") ||
            version.StartsWith("1.17") || version.StartsWith("1.16")) return "WraithMod-1.19.jar";
        return null; // 1.14 / 1.15 — Fabric supported but no dedicated build yet
    }

    private void Stop_Click(object sender, RoutedEventArgs e)
    {
        _launcher.KillGame();
        Log("[Wraith] Game stopped.");
        ResetPlay();
    }

    private void OnProgress(LaunchProgress prog)
    {
        Dispatcher.Invoke(() =>
        {
            SetStatus(prog.Status, !prog.IsDownloading);
            LaunchProgress.Value = prog.Percent;
            if (!string.IsNullOrEmpty(prog.Status)) Log($"[Wraith] {prog.Status}");
        });
    }

    private void OnLog(string line) => Dispatcher.Invoke(() => Log(line));

    private void OnExited()
    {
        Dispatcher.Invoke(() =>
        {
            Log("[Wraith] Game exited.");
            App.Discord.SetIdle();
            Application.Current.MainWindow?.Show();
            ResetPlay();
        });
    }

    private void ResetPlay()
    {
        _gameRunning = false;
        PlayBtn.IsEnabled         = true;
        PlayBtn.Content           = "PLAY";
        StopBtn.Visibility        = Visibility.Collapsed;
        LaunchProgress.Visibility = Visibility.Collapsed;
        LaunchProgress.Value      = 0;
        SetStatus("Ready", true);
    }

    private void SetStatus(string text, bool ok)
    {
        StatusText.Text       = text;
        var brush = ok
            ? (SolidColorBrush)FindResource("SuccessBrush")
            : (SolidColorBrush)FindResource("WarningBrush");
        StatusText.Foreground = brush;
        StatusDot.Fill        = brush;
    }

    private void CopyLog_Click(object sender, RoutedEventArgs e)
    {
        if (!string.IsNullOrEmpty(ConsoleText.Text))
            Clipboard.SetText(ConsoleText.Text);
    }

    private void Log(string line)
    {
        var lines = ConsoleText.Text.Split('\n').TakeLast(59).Append(line).ToArray();
        ConsoleText.Text = string.Join('\n', lines);
        ConsoleScroll.ScrollToEnd();
    }
}
