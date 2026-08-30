using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Animation;
using System.Windows.Threading;

namespace WraithClient;

public partial class SplashWindow : Window
{
    private readonly DispatcherTimer _timer = new();
    private double _progress = 0;
    private int    _step     = 0;

    private static readonly (string text, double target)[] Steps =
    [
        ("Loading resources...",    18),
        ("Checking Java...",        36),
        ("Verifying mod files...",  55),
        ("Connecting services...",  72),
        ("Preparing launcher...",   88),
        ("Ready.",                 100),
    ];

    public SplashWindow()
    {
        InitializeComponent();
        Opacity = 0;
        Loaded += OnLoaded;
    }

    private void OnLoaded(object sender, RoutedEventArgs e)
    {
        // Fade in
        BeginAnimation(OpacityProperty,
            new DoubleAnimation(0, 1, TimeSpan.FromMilliseconds(350))
            { EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut } });

        _timer.Interval = TimeSpan.FromMilliseconds(16);
        _timer.Tick     += Tick;
        _timer.Start();
    }

    private void Tick(object? sender, EventArgs e)
    {
        if (_step >= Steps.Length) return;

        double target = Steps[_step].target;
        _progress += (target - _progress) * 0.08;

        double trackW = ((Border)ProgressFill.Parent).ActualWidth;
        ProgressFill.Width = trackW * (_progress / 100.0);
        StatusText.Text    = Steps[_step].text;

        if (_progress >= target - 0.3) _step++;

        if (_step >= Steps.Length && _progress >= 99.5)
        {
            _timer.Stop();
            ProgressFill.Width = trackW;
            Dispatcher.InvokeAsync(FinishSplash, DispatcherPriority.Background);
        }
    }

    private void FinishSplash()
    {
        var main = new MainWindow();
        main.Opacity = 0;
        Application.Current.MainWindow = main;
        main.Show();

        // Fade main in while splash fades out
        main.BeginAnimation(OpacityProperty,
            new DoubleAnimation(0, 1, TimeSpan.FromMilliseconds(400))
            { EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut } });

        var fadeOut = new DoubleAnimation(1, 0, TimeSpan.FromMilliseconds(300));
        fadeOut.Completed += (_, _) => Close();
        BeginAnimation(OpacityProperty, fadeOut);
    }
}
