using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Animation;
using WraithClient.Services;

namespace WraithClient.Views;

public partial class LaunchProfilesView : Page
{
    private List<LaunchProfile> _profiles = [];
    private LaunchProfile? _editing;
    private bool _panelOpen;

    private static readonly string[] McVersions =
    [
        // 26.x (year-based versioning)
        "26.2", "26.1.2", "26.1.1", "26.1",
        // 1.21.x
        "1.21.11", "1.21.10", "1.21.9", "1.21.8", "1.21.7", "1.21.6",
        "1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21",
        // 1.20.x
        "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20",
        // 1.19.x
        "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19",
        // 1.18.x
        "1.18.2", "1.18.1", "1.18",
        // 1.17.x
        "1.17.1", "1.17",
        // 1.16.x
        "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16",
        // 1.15.x
        "1.15.2", "1.15.1", "1.15",
        // 1.14.x
        "1.14.4", "1.14.3", "1.14.2", "1.14.1", "1.14",
        // 1.13.x
        "1.13.2", "1.13.1", "1.13",
        // 1.12.x
        "1.12.2", "1.12.1", "1.12",
        // 1.11.x
        "1.11.2", "1.11.1", "1.11",
        // 1.10.x
        "1.10.2", "1.10.1", "1.10",
        // 1.9.x
        "1.9.4", "1.9.3", "1.9.2", "1.9.1", "1.9",
        // 1.8.x
        "1.8.9",
    ];

    private static readonly string[] Loaders = ["Fabric", "Quilt", "NeoForge", "Forge", "Vanilla"];

    public LaunchProfilesView()
    {
        InitializeComponent();
        _profiles = ProfileService.Load();
        Render();
    }

    private void Render()
    {
        ProfileList.Children.Clear();
        int active = _profiles.Count(p => p.IsActive);
        SubText.Text = $"{_profiles.Count} profile{(_profiles.Count != 1 ? "s" : "")}  •  {active} active";

        foreach (var p in _profiles)
            ProfileList.Children.Add(BuildCard(p));
    }

    private UIElement BuildCard(LaunchProfile profile)
    {
        var card = new Border
        {
            Background      = new SolidColorBrush(Color.FromRgb(0x11, 0x11, 0x11)),
            BorderBrush     = new SolidColorBrush(profile.IsActive
                ? Color.FromRgb(0xC0, 0xC0, 0xC0)
                : Color.FromRgb(0x22, 0x22, 0x22)),
            BorderThickness = new Thickness(1),
            CornerRadius    = new CornerRadius(10),
            Padding         = new Thickness(16, 14, 16, 14),
            Margin          = new Thickness(0, 0, 0, 8),
            Cursor          = System.Windows.Input.Cursors.Hand
        };
        card.PreviewMouseLeftButtonDown += (_, _) => OpenEdit(profile);

        var grid = new Grid();
        grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

        var left = new StackPanel();

        var nameRow = new StackPanel { Orientation = Orientation.Horizontal };
        nameRow.Children.Add(new TextBlock
        {
            Text       = profile.Name,
            Foreground = new SolidColorBrush(Color.FromRgb(0xEC, 0xEC, 0xEC)),
            FontSize   = 14, FontWeight = FontWeights.SemiBold
        });
        if (profile.IsActive)
            nameRow.Children.Add(ActiveBadge());
        left.Children.Add(nameRow);

        left.Children.Add(new TextBlock
        {
            Text       = $"{profile.ModLoader}  •  MC {profile.MinecraftVersion}  •  {profile.MemoryMB} MB RAM",
            Foreground = new SolidColorBrush(Color.FromRgb(0x44, 0x44, 0x44)),
            FontSize   = 11, Margin = new Thickness(0, 4, 0, 0)
        });

        if (!string.IsNullOrWhiteSpace(profile.GameDirectory))
            left.Children.Add(new TextBlock
            {
                Text       = profile.GameDirectory,
                Foreground = new SolidColorBrush(Color.FromRgb(0x33, 0x33, 0x33)),
                FontSize   = 10, Margin = new Thickness(0, 2, 0, 0),
                TextTrimming = System.Windows.TextTrimming.CharacterEllipsis
            });

        Grid.SetColumn(left, 0);
        grid.Children.Add(left);

        var btns = new StackPanel { Orientation = Orientation.Horizontal, VerticalAlignment = VerticalAlignment.Center };

        if (!profile.IsActive)
        {
            var setActive = Btn("Set Active", 0x5C, 0xB8, 0x5C);
            setActive.Click += (_, _) =>
            {
                foreach (var p in _profiles) p.IsActive = false;
                profile.IsActive = true;
                ProfileService.Save(_profiles);
                Render();
            };
            btns.Children.Add(setActive);
        }

        var editBtn = Btn("Edit", 0x77, 0x77, 0x77);
        editBtn.Click += (_, _) => OpenEdit(profile);
        btns.Children.Add(editBtn);

        if (_profiles.Count > 1)
        {
            var del = Btn("Delete", 0xC8, 0x50, 0x50);
            del.Click += (_, _) =>
            {
                _profiles.Remove(profile);
                if (!_profiles.Any(p => p.IsActive)) _profiles[0].IsActive = true;
                ProfileService.Save(_profiles);
                CloseEditImmediate();
                Render();
            };
            btns.Children.Add(del);
        }

        Grid.SetColumn(btns, 1);
        grid.Children.Add(btns);
        card.Child = grid;
        return card;
    }

    private void OpenEdit(LaunchProfile profile)
    {
        _editing = profile;
        EditTitle.Text = $"Edit  —  {profile.Name}";
        BuildEditForm(profile);
        SlideEdit(true);
    }

    private void BuildEditForm(LaunchProfile profile)
    {
        EditPanel.Children.Clear();

        EditPanel.Children.Add(FieldLabel("PROFILE NAME"));
        var nameBox = Field(profile.Name);
        nameBox.TextChanged += (_, _) => { profile.Name = nameBox.Text; };
        EditPanel.Children.Add(nameBox);

        EditPanel.Children.Add(FieldLabel("MINECRAFT VERSION"));
        var mcCombo = Combo(McVersions, profile.MinecraftVersion);
        mcCombo.SelectionChanged += (_, _) =>
        {
            if (mcCombo.SelectedItem is string v) profile.MinecraftVersion = v;
        };
        EditPanel.Children.Add(mcCombo);

        EditPanel.Children.Add(FieldLabel("MOD LOADER"));
        var loaderCombo = Combo(Loaders, profile.ModLoader);
        loaderCombo.SelectionChanged += (_, _) =>
        {
            if (loaderCombo.SelectedItem is string l) profile.ModLoader = l;
        };
        EditPanel.Children.Add(loaderCombo);

        EditPanel.Children.Add(FieldLabel("MOD LOADER VERSION  (leave blank for latest)"));
        var loaderVerBox = Field(profile.ModLoaderVersion);
        loaderVerBox.TextChanged += (_, _) => profile.ModLoaderVersion = loaderVerBox.Text;
        EditPanel.Children.Add(loaderVerBox);

        EditPanel.Children.Add(FieldLabel("GAME DIRECTORY  (leave blank for default)"));
        var dirBox = Field(profile.GameDirectory);
        dirBox.TextChanged += (_, _) => profile.GameDirectory = dirBox.Text;
        EditPanel.Children.Add(dirBox);

        EditPanel.Children.Add(FieldLabel($"MEMORY  —  {profile.MemoryMB} MB"));
        var memLabel = (TextBlock)EditPanel.Children[EditPanel.Children.Count - 1];
        var slider = new Slider
        {
            Minimum = 512, Maximum = 16384, Value = profile.MemoryMB,
            TickFrequency = 512, IsSnapToTickEnabled = true,
            Margin = new Thickness(0, 4, 0, 20),
            Foreground = new SolidColorBrush(Color.FromRgb(0xD0, 0xD0, 0xD0))
        };
        slider.ValueChanged += (_, e) =>
        {
            profile.MemoryMB = (int)e.NewValue;
            memLabel.Text = $"MEMORY  —  {profile.MemoryMB} MB";
        };
        EditPanel.Children.Add(slider);

        var saveBtn = new Button
        {
            Content = "Save Profile",
            Style   = (Style)Application.Current.FindResource("PrimaryBtn"),
            Height  = 40, HorizontalAlignment = HorizontalAlignment.Stretch, Padding = new Thickness(0)
        };
        saveBtn.Click += (_, _) =>
        {
            EditTitle.Text = $"Edit  —  {profile.Name}";
            ProfileService.Save(_profiles);
            Render();
        };
        EditPanel.Children.Add(saveBtn);
    }

    private void NewProfile_Click(object sender, RoutedEventArgs e)
    {
        var p = new LaunchProfile { Name = $"Profile {_profiles.Count + 1}", MinecraftVersion = "1.21.4", ModLoader = "Fabric", MemoryMB = 2048 };
        _profiles.Add(p);
        ProfileService.Save(_profiles);
        Render();
        OpenEdit(p);
    }

    private void CloseEdit_Click(object sender, RoutedEventArgs e) => SlideEdit(false);

    private void CloseEditImmediate()
    {
        if (!_panelOpen) return;
        _panelOpen = false;
        EditCol.Width = new GridLength(0);
        EditBorder.RenderTransform = new TranslateTransform(0, 0);
    }

    private void SlideEdit(bool open)
    {
        if (!open && !_panelOpen) return;
        var t = EditBorder.RenderTransform as TranslateTransform ?? new TranslateTransform();
        EditBorder.RenderTransform = t;
        if (open)
        {
            EditCol.Width = new GridLength(340);
            _panelOpen = true;
            t.X = 340;
            var a = new DoubleAnimation(340, 0, new Duration(TimeSpan.FromMilliseconds(220)))
                { EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut } };
            t.BeginAnimation(TranslateTransform.XProperty, a);
        }
        else
        {
            _panelOpen = false;
            var a = new DoubleAnimation(0, 340, new Duration(TimeSpan.FromMilliseconds(180)))
                { EasingFunction = new CubicEase { EasingMode = EasingMode.EaseIn } };
            a.Completed += (_, _) => EditCol.Width = new GridLength(0);
            t.BeginAnimation(TranslateTransform.XProperty, a);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static Border ActiveBadge() => new()
    {
        Background      = new SolidColorBrush(Color.FromRgb(0x1A, 0x2A, 0x1A)),
        BorderBrush     = new SolidColorBrush(Color.FromRgb(0x5C, 0xB8, 0x5C)),
        BorderThickness = new Thickness(1),
        CornerRadius    = new CornerRadius(4),
        Padding         = new Thickness(7, 1, 7, 1),
        Margin          = new Thickness(10, 0, 0, 0),
        VerticalAlignment = VerticalAlignment.Center,
        Child = new TextBlock { Text = "ACTIVE", Foreground = new SolidColorBrush(Color.FromRgb(0x5C, 0xB8, 0x5C)), FontSize = 9, FontWeight = FontWeights.SemiBold }
    };

    private static Button Btn(string label, byte r, byte g, byte b)
    {
        var btn = new Button
        {
            Content    = label,
            Style      = (Style)Application.Current.FindResource("GhostBtn"),
            Padding    = new Thickness(12, 5, 12, 5),
            Margin     = new Thickness(6, 0, 0, 0),
            Foreground = new SolidColorBrush(Color.FromRgb(r, g, b))
        };
        return btn;
    }

    private static TextBlock FieldLabel(string text) => new()
    {
        Text       = text,
        Foreground = new SolidColorBrush(Color.FromRgb(0x44, 0x44, 0x44)),
        FontSize   = 10, FontWeight = FontWeights.SemiBold,
        Margin     = new Thickness(0, 14, 0, 6)
    };

    private static TextBox Field(string value) => new()
    {
        Text    = value,
        Style   = (Style)Application.Current.FindResource("TextBox"),
        Height  = 36
    };

    private static ComboBox Combo(string[] items, string selected)
    {
        var cb = new ComboBox { Style = (Style)Application.Current.FindResource("Combo"), Height = 36 };
        foreach (var item in items) cb.Items.Add(item);
        cb.SelectedItem = selected;
        return cb;
    }
}
