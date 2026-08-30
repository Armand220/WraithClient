using System.Threading;
using System.Windows;
using AutoUpdaterDotNET;
using WraithClient.Models;
using WraithClient.Services;

namespace WraithClient;

public partial class App : Application
{
    public static AppSettings Settings { get; internal set; } = null!;
    public static DiscordRPCService Discord { get; private set; } = null!;

    private static Mutex? _instanceMutex;

    private void App_Startup(object sender, StartupEventArgs e)
    {
        AppDomain.CurrentDomain.UnhandledException += (_, ex) =>
        {
            var msg = ex.ExceptionObject?.ToString() ?? "unknown";
            var path = System.IO.Path.Combine(AppContext.BaseDirectory, "crash.log");
            System.IO.File.WriteAllText(path, msg);
            MessageBox.Show($"Fatal crash:\n{msg.Split('\n')[0]}", "Wraith Client",
                MessageBoxButton.OK, MessageBoxImage.Error);
        };

        bool isNew;
        try
        {
            _instanceMutex = new Mutex(true, "WraithClient_SingleInstance", out isNew);
        }
        catch (AbandonedMutexException)
        {
            // Previous instance crashed mid-run; treat as a fresh start
            isNew = true;
        }
        if (!isNew)
        {
            MessageBox.Show("Wraith Client is already running.", "Wraith Client",
                MessageBoxButton.OK, MessageBoxImage.Information);
            Shutdown();
            return;
        }

        Settings = SettingsService.Load();
        Discord = new DiscordRPCService();

        if (Settings.DiscordRpc)
            Discord.Start();

        DispatcherUnhandledException += (_, ex) =>
        {
            MessageBox.Show($"Unhandled error:\n{ex.Exception.Message}",
                "Wraith Client Error", MessageBoxButton.OK, MessageBoxImage.Error);
            ex.Handled = true;
        };

        // Check for updates in the background — only shows a dialog if a newer version exists
        AutoUpdater.AppTitle   = "Wraith Client";
        AutoUpdater.RunUpdateAsAdmin = false;
        AutoUpdater.Start("https://raw.githubusercontent.com/Armand220/WraithClient/main/update.xml");

        var splash = new SplashWindow();
        MainWindow = splash;
        splash.Show();
    }

    protected override void OnExit(ExitEventArgs e)
    {
        SettingsService.Save(Settings);
        Discord.Dispose();
        _instanceMutex?.ReleaseMutex();
        _instanceMutex?.Dispose();
        base.OnExit(e);
    }
}
