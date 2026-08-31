using System.IO;
using CmlLib.Core;
using CmlLib.Core.Auth;
using CmlLib.Core.ProcessBuilder;
using WraithClient.Models;

namespace WraithClient.Services;

public class LaunchProgress
{
    public string Status { get; init; } = string.Empty;
    public double Percent { get; init; }
    public bool IsDownloading { get; init; }
}

public class MinecraftLaunchService
{
    public event Action<LaunchProgress>? ProgressChanged;
    public event Action<string>? LogReceived;
    public event Action? GameExited;

    private System.Diagnostics.Process? _gameProcess;

    public async Task<bool> LaunchAsync(
        AppSettings settings,
        AuthResult auth,
        string? versionOverride = null,
        string? gameDirOverride = null,
        CancellationToken ct = default)
    {
        var gameDir = gameDirOverride
            ?? (string.IsNullOrEmpty(settings.GameDirectory)
                ? SettingsService.GetDefaultGameDir()
                : settings.GameDirectory);

        Directory.CreateDirectory(gameDir);

        var path    = new MinecraftPath(gameDir);
        var launcher = new MinecraftLauncher(path);

        launcher.FileProgressChanged += (_, e) =>
        {
            var pct = e.TotalTasks == 0 ? 0.0 : (double)e.ProgressedTasks / e.TotalTasks * 85;
            ProgressChanged?.Invoke(new LaunchProgress
            {
                Status        = e.Name ?? "Preparing...",
                Percent       = pct,
                IsDownloading = true
            });
        };

        launcher.ByteProgressChanged += (_, e) =>
        {
            if (e.TotalBytes == 0) return;
            ProgressChanged?.Invoke(new LaunchProgress
            {
                Status        = "Downloading...",
                Percent       = (double)e.ProgressedBytes / e.TotalBytes * 85,
                IsDownloading = true
            });
        };

        Report("Preparing...", 0);

        var session = auth.IsOffline()
            ? MSession.CreateOfflineSession(auth.Username ?? settings.Username)
            : new MSession(auth.Username!, auth.AccessToken!, auth.Uuid!);

        var option = new MLaunchOption
        {
            Session      = session,
            MaximumRamMb = settings.MaxRamMb,
            MinimumRamMb = settings.MinRamMb,
            FullScreen   = settings.Fullscreen,
            ScreenWidth  = settings.Fullscreen ? 0 : settings.WindowWidth,
            ScreenHeight = settings.Fullscreen ? 0 : settings.WindowHeight,
        };

        if (!string.IsNullOrEmpty(settings.QuickConnectIp))
        {
            option.ServerIp   = settings.QuickConnectIp;
            option.ServerPort = settings.QuickConnectPort;
        }

        if (!string.IsNullOrEmpty(settings.JavaPath))
            option.JavaPath = settings.JavaPath;

        if (!string.IsNullOrWhiteSpace(settings.JvmArgs))
            option.JvmArgumentOverrides = BuildJvmArgs(settings)
                .Select(s => new MArgument(s));

        Report("Downloading / verifying files...", 5);

        var process = await launcher.InstallAndBuildProcessAsync(versionOverride ?? settings.SelectedVersion, option);

        process.StartInfo.RedirectStandardOutput = true;
        process.StartInfo.RedirectStandardError  = true;
        process.StartInfo.UseShellExecute        = false;
        process.EnableRaisingEvents              = true;

        process.OutputDataReceived += (_, e) => { if (e.Data != null) LogReceived?.Invoke(e.Data); };
        process.ErrorDataReceived  += (_, e) => { if (e.Data != null) LogReceived?.Invoke($"[ERR] {e.Data}"); };
        process.Exited             += (_, _)  => GameExited?.Invoke();

        Report("Launching...", 95);

        _gameProcess = process;
        process.Start();
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();

        Report("Game running", 100);
        return true;
    }

    public void KillGame()
    {
        try { _gameProcess?.Kill(entireProcessTree: true); } catch { }
    }

    private void Report(string status, double pct) =>
        ProgressChanged?.Invoke(new LaunchProgress { Status = status, Percent = pct, IsDownloading = pct < 100 });

    private static string[] BuildJvmArgs(AppSettings settings)
    {
        var args = new List<string>
        {
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",
        };

        foreach (var arg in settings.JvmArgs.Split(' ', StringSplitOptions.RemoveEmptyEntries))
            if (!args.Contains(arg)) args.Add(arg);

        return args.ToArray();
    }
}
