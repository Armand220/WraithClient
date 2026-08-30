using System.Windows;
using System.Windows.Controls;
using WraithClient.Services;

namespace WraithClient.Views;

public partial class ProfileView : Page
{
    private readonly MainWindow _main;

    public ProfileView(MainWindow main)
    {
        _main = main;
        InitializeComponent();
        Loaded += (_, _) => UsernameBox.Text = App.Settings.Username;
    }

    private void SaveUsername_Click(object sender, RoutedEventArgs e)
    {
        var name = UsernameBox.Text.Trim();
        if (name.Length < 2) { UsernameStatus.Text = "Must be at least 2 characters."; return; }

        App.Settings.Username = name;
        SettingsService.Save(App.Settings);
        UsernameStatus.Text = $"Saved as \"{name}\".";
        _main.RefreshPage("Home");
    }
}
