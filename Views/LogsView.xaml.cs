using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace WraithClient.Views;

public partial class LogsView : Page
{
    private FileSystemWatcher? _watcher;
    private string? _logPath;
    private long _lastPos;
    private string _filter = "ALL";
    private readonly List<(string raw, string level)> _buffer = [];

    public LogsView()
    {
        InitializeComponent();
        Loaded   += (_, _) => StartWatching();
        Unloaded += (_, _) => StopWatching();
    }

    private void StartWatching()
    {
        _logPath = FindLogFile();
        if (_logPath == null)
        {
            LogPathText.Text = "Log file not found — launch Minecraft first";
            SetLive(false);
            return;
        }

        LogPathText.Text = _logPath;
        _lastPos = 0;
        ReadNewLines();

        _watcher = new FileSystemWatcher(Path.GetDirectoryName(_logPath)!, Path.GetFileName(_logPath))
        {
            NotifyFilter = NotifyFilters.LastWrite | NotifyFilters.Size,
            EnableRaisingEvents = true
        };
        _watcher.Changed += (_, _) => Dispatcher.Invoke(ReadNewLines);
        SetLive(true);
    }

    private void StopWatching()
    {
        _watcher?.Dispose();
        _watcher = null;
    }

    private void ReadNewLines()
    {
        if (_logPath == null || !File.Exists(_logPath)) return;
        try
        {
            using var fs = new FileStream(_logPath, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
            fs.Seek(_lastPos, SeekOrigin.Begin);
            using var reader = new StreamReader(fs);
            string? line;
            while ((line = reader.ReadLine()) != null)
            {
                var level = DetectLevel(line);
                _buffer.Add((line, level));
                if (MatchesFilter(level))
                    AppendLine(line, level);
            }
            _lastPos = fs.Position;
            LogScroll.ScrollToEnd();
        }
        catch { }
    }

    private void AppendLine(string text, string level)
    {
        var color = level switch
        {
            "ERROR" => Color.FromRgb(0xC8, 0x50, 0x50),
            "WARN"  => Color.FromRgb(0xC8, 0x90, 0x40),
            "INFO"  => Color.FromRgb(0xAA, 0xAA, 0xAA),
            _       => Color.FromRgb(0x55, 0x55, 0x55)
        };

        LogLines.Children.Add(new TextBlock
        {
            Text            = text,
            Foreground      = new SolidColorBrush(color),
            FontFamily      = new FontFamily("Consolas, Courier New"),
            FontSize        = 11,
            TextWrapping    = TextWrapping.Wrap,
            Margin          = new Thickness(0, 0, 0, 1)
        });

        // Cap at 2000 visible lines to keep memory sane
        while (LogLines.Children.Count > 2000)
            LogLines.Children.RemoveAt(0);
    }

    private void Clear_Click(object sender, RoutedEventArgs e)
    {
        LogLines.Children.Clear();
        _buffer.Clear();
        _lastPos = 0;
    }

    private void OpenFile_Click(object sender, RoutedEventArgs e)
    {
        if (_logPath != null && File.Exists(_logPath))
            System.Diagnostics.Process.Start("explorer.exe", $"/select,\"{_logPath}\"");
    }

    private void Filter_Click(object sender, System.Windows.Input.MouseButtonEventArgs e)
    {
        if (sender is not Border b || b.Tag is not string tag) return;
        _filter = tag;

        // Update chip styles
        foreach (var chip in new[] { FilterAll, FilterInfo, FilterWarn, FilterError })
        {
            bool active = chip.Tag as string == _filter;
            chip.Background   = new SolidColorBrush(active
                ? Color.FromArgb(0x1E, 0xFF, 0xFF, 0xFF)
                : Color.FromRgb(0x11, 0x11, 0x11));
            chip.BorderBrush  = new SolidColorBrush(active
                ? Color.FromRgb(0x44, 0x44, 0x44)
                : Color.FromRgb(0x22, 0x22, 0x22));
        }

        // Rebuild visible lines from buffer
        LogLines.Children.Clear();
        foreach (var (raw, level) in _buffer)
            if (MatchesFilter(level))
                AppendLine(raw, level);

        LogScroll.ScrollToEnd();
    }

    private bool MatchesFilter(string level) =>
        _filter == "ALL" || level == _filter;

    private static string DetectLevel(string line)
    {
        if (line.Contains("[ERROR]") || line.Contains("/ERROR") || line.Contains("ERROR:")) return "ERROR";
        if (line.Contains("[WARN]")  || line.Contains("/WARN")  || line.Contains("WARN:"))  return "WARN";
        if (line.Contains("[INFO]")  || line.Contains("/INFO")  || line.Contains("INFO:"))  return "INFO";
        return "DEBUG";
    }

    private static string? FindLogFile()
    {
        var gameDir = string.IsNullOrEmpty(App.Settings.GameDirectory)
            ? Services.SettingsService.GetDefaultGameDir()
            : App.Settings.GameDirectory;

        var candidates = new[]
        {
            Path.Combine(gameDir, "logs", "latest.log"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                         ".minecraft", "logs", "latest.log")
        };

        return candidates.FirstOrDefault(File.Exists);
    }

    private void SetLive(bool live)
    {
        LiveDot.Background = new SolidColorBrush(live
            ? Color.FromRgb(0x5C, 0xB8, 0x5C)
            : Color.FromRgb(0x44, 0x44, 0x44));
        LiveText.Text = live ? "Live" : "Offline";
        LiveText.Foreground = new SolidColorBrush(live
            ? Color.FromRgb(0x5C, 0xB8, 0x5C)
            : Color.FromRgb(0x44, 0x44, 0x44));
    }
}
