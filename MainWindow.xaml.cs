using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Interop;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using WraithClient.Views;

namespace WraithClient;

public partial class MainWindow : Window
{
    private readonly Dictionary<string, Page> _pages = new();
    private string _current = "";

    // DWM rounded corners (Windows 11+)
    [DllImport("dwmapi.dll")]
    private static extern int DwmSetWindowAttribute(IntPtr hwnd, int attr, ref int value, int size);
    private const int DWMWA_WINDOW_CORNER_PREFERENCE = 33;
    private const int DWMWCP_ROUND = 2;

    public MainWindow()
    {
        InitializeComponent();
        SidebarVer.Text = $"v1.0  •  MC {App.Settings.SelectedVersion}";
        SourceInitialized += (_, _) => ApplyDwmRoundedCorners();

        var s = App.Settings;
        Width  = s.LauncherWidth;
        Height = s.LauncherHeight;
        SidebarCol.Width = new System.Windows.GridLength(s.SidebarWidth);

        NavigateTo("Home");
    }

    public void NavigateTo(string page)
    {
        if (_current == page) return;
        _current = page;

        if (!_pages.TryGetValue(page, out var p))
        {
            p = page switch
            {
                "Home"      => new HomeView(this),
                "Profile"   => new ProfileView(this),
                "Settings"  => new SettingsView(),
                "Profiles"  => new LaunchProfilesView(),
                "Mods"      => new ModsView(),
                "Cheats"    => new CheatsView(),
                "Logs"      => new LogsView(),
                _           => new HomeView(this)
            };
            _pages[page] = p;
        }

        // Fade out → navigate → fade in
        var fadeOut = new DoubleAnimation(1, 0, TimeSpan.FromMilliseconds(80));
        fadeOut.Completed += (_, _) =>
        {
            PageFrame.Navigate(p);
            var fadeIn = new DoubleAnimation(0, 1, TimeSpan.FromMilliseconds(160));
            fadeIn.EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut };
            PageFrame.BeginAnimation(OpacityProperty, fadeIn);
        };
        PageFrame.BeginAnimation(OpacityProperty, fadeOut);
        UpdateNav(page);
    }

    private static readonly string[] NavOrder =
        ["Home", "Profile", "Settings", "Profiles", "Mods", "Cheats", "Logs"];

    private bool _navInitialized;

    private void UpdateNav(string active)
    {
        // Slide the highlight to the active tab
        int idx = Array.IndexOf(NavOrder, active);
        if (idx >= 0)
        {
            double targetY = idx * 52.0;
            if (!_navInitialized)
            {
                NavHighlightTransform.Y = targetY;
                _navInitialized = true;
            }
            else
            {
                var anim = new DoubleAnimation(NavHighlightTransform.Y, targetY,
                    new Duration(TimeSpan.FromMilliseconds(220)))
                {
                    EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut }
                };
                NavHighlightTransform.BeginAnimation(TranslateTransform.YProperty, anim);
            }
        }

        // Update icon fills
        SetIconFill(IcoHome,      active == "Home");
        SetIconFill(IcoProfile,   active == "Profile");
        SetIconFill(IcoSettings,  active == "Settings");
        SetIconFill(IcoProfiles,  active == "Profiles");
        SetIconFill(IcoMods,      active == "Mods");
        SetIconFill(IcoCheats,    active == "Cheats");
        SetIconFill(IcoLogs,      active == "Logs");
    }

    private static void SetIconFill(Path icon, bool active)
    {
        if (icon.Fill is not SolidColorBrush brush || brush.IsFrozen)
        {
            icon.Fill = new SolidColorBrush(active ? Colors.White : Color.FromRgb(0x55, 0x55, 0x55));
            return;
        }
        var target = active ? Colors.White : Color.FromRgb(0x55, 0x55, 0x55);
        brush.BeginAnimation(SolidColorBrush.ColorProperty,
            new ColorAnimation(target, new Duration(TimeSpan.FromMilliseconds(150))));
    }

    public void ApplySidebarWidth(double width, bool showLabels)
    {
        SidebarCol.Width = new System.Windows.GridLength(width);
    }

    private void Nav_Click(object sender, RoutedEventArgs e)
    {
        if (sender is Button b && b.Tag is string tag)
            NavigateTo(tag);
    }

    public void RefreshPage(string page)
    {
        _pages.Remove(page);
        if (_current == page) { _current = ""; NavigateTo(page); }
    }

    public void UpdateVersionBadge() =>
        SidebarVer.Text = $"v1.0  •  MC {App.Settings.SelectedVersion}";

    private void ApplyDwmRoundedCorners()
    {
        try
        {
            var hwnd = new WindowInteropHelper(this).Handle;
            int pref = DWMWCP_ROUND;
            DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, ref pref, sizeof(int));
        }
        catch { /* older Windows — no native rounding, fine */ }
    }

    private void TitleBar_MouseDown(object sender, MouseButtonEventArgs e)
    {
        if (e.ClickCount == 2) Maximize_Click(sender, e);
        else if (e.LeftButton == MouseButtonState.Pressed) DragMove();
    }

    private void Minimize_Click(object sender, RoutedEventArgs e) =>
        WindowState = WindowState.Minimized;

    private void Maximize_Click(object sender, RoutedEventArgs e) =>
        WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;

    private void Close_Click(object sender, RoutedEventArgs e) => Close();
}
