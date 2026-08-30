using System.IO;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Win32;
using WraithClient.Services;

namespace WraithClient.Views;

public partial class SettingsView : Page
{
    public SettingsView()
    {
        InitializeComponent();
        Loaded += (_, _) => LoadSettings();
    }

    private void LoadSettings()
    {
        var s = App.Settings;

        UsernameBox.Text = s.Username;
        GameDirBox.Text  = s.GameDirectory;
        JavaBox.Text     = s.JavaPath ?? "";
        RamSlider.Value  = s.MaxRamMb;
        RamLabel.Text    = $"{s.MaxRamMb} MB";
        JvmBox.Text      = s.JvmArgs ?? "";

        FullscreenCheck.IsChecked = s.Fullscreen;
        WidthBox.Text  = s.WindowWidth.ToString();
        HeightBox.Text = s.WindowHeight.ToString();

        DiscordCheck.IsChecked   = s.DiscordRpc;
        KeepOpenCheck.IsChecked  = s.KeepLauncherOpen;
        InjectModCheck.IsChecked = s.InjectWraithMod;

        ResGrid.IsEnabled = !s.Fullscreen;
    }

    private void RamSlider_Changed(object sender, RoutedPropertyChangedEventArgs<double> e)
    {
        if (RamLabel != null)
            RamLabel.Text = $"{(int)RamSlider.Value} MB";
    }

    private void Fullscreen_Changed(object sender, RoutedEventArgs e)
    {
        if (ResGrid != null)
            ResGrid.IsEnabled = FullscreenCheck.IsChecked != true;
    }

    private void BrowseDir_Click(object sender, RoutedEventArgs e)
    {
        var dlg = new OpenFolderDialog
        {
            Title = "Select game directory"
        };
        if (dlg.ShowDialog() == true)
            GameDirBox.Text = dlg.FolderName;
    }

    private void BrowseJava_Click(object sender, RoutedEventArgs e)
    {
        var dlg = new OpenFileDialog
        {
            Title  = "Select java.exe or javaw.exe",
            Filter = "Java|java.exe;javaw.exe|All files|*.*"
        };
        if (dlg.ShowDialog() == true)
            JavaBox.Text = dlg.FileName;
    }

    private void Save_Click(object sender, RoutedEventArgs e)
    {
        var s = App.Settings;

        s.Username      = UsernameBox.Text.Trim();
        s.GameDirectory = GameDirBox.Text.Trim();
        s.JavaPath      = string.IsNullOrWhiteSpace(JavaBox.Text) ? null : JavaBox.Text.Trim();
        s.MaxRamMb      = (int)RamSlider.Value;

        s.JvmArgs = JvmBox.Text.Trim();

        s.Fullscreen      = FullscreenCheck.IsChecked == true;
        s.WindowWidth     = int.TryParse(WidthBox.Text, out var w) ? w : 854;
        s.WindowHeight    = int.TryParse(HeightBox.Text, out var h) ? h : 480;

        bool discordWasOff = !s.DiscordRpc;
        s.DiscordRpc       = DiscordCheck.IsChecked   == true;
        s.KeepLauncherOpen = KeepOpenCheck.IsChecked  == true;
        s.InjectWraithMod  = InjectModCheck.IsChecked == true;

        if (s.DiscordRpc && discordWasOff)
            App.Discord.Start();
        else if (!s.DiscordRpc)
            App.Discord.Stop();

        SettingsService.Save(s);
        SaveStatus.Text = $"Saved at {DateTime.Now:HH:mm:ss}";
    }

    private void Reset_Click(object sender, RoutedEventArgs e)
    {
        var res = MessageBox.Show(
            "Reset all settings to defaults?",
            "Wraith Client",
            MessageBoxButton.YesNo,
            MessageBoxImage.Question);

        if (res != MessageBoxResult.Yes) return;

        App.Settings = new Models.AppSettings();
        SettingsService.Save(App.Settings);
        LoadSettings();
        SaveStatus.Text = "Reset to defaults.";
    }
}
