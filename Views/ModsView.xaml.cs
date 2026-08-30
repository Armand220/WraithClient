using System.IO;
using System.Net.Http;
using System.Net.Http.Json;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Media.Imaging;
using WraithClient.Services;

namespace WraithClient.Views;

public partial class ModsView : Page
{
    private static readonly HttpClient Http = new()
    {
        DefaultRequestHeaders = { { "User-Agent", "WraithClient/1.0 (wraith-launcher)" } }
    };

    private static readonly string[] Categories =
        ["optimization", "utility", "library", "decoration", "technology", "magic", "adventure", "food"];

    private string? _activeCategory;
    private bool _detailOpen;
    private bool _browseActive = true;
    private int  _offset;
    private int  _totalHits;
    private bool _loadingMore;

    public ModsView()
    {
        InitializeComponent();
        BuildCategoryChips();
        SetTab(browse: true);
        Loaded += async (_, _) => await SearchAsync("");
    }

    // ── Tabs ─────────────────────────────────────────────────────────────

    private void TabBrowse_Click(object sender, RoutedEventArgs e)   => SetTab(browse: true);
    private void TabInstalled_Click(object sender, RoutedEventArgs e) => SetTab(browse: false);

    private void SetTab(bool browse)
    {
        _browseActive = browse;

        var active   = (Style)Application.Current.FindResource("PrimaryBtn");
        var inactive = (Style)Application.Current.FindResource("GhostBtn");

        // Swap tab styles
        TabBrowse.Style    = browse  ? active : inactive;
        TabInstalled.Style = !browse ? active : inactive;

        // Swap tab styles foreground for PrimaryBtn (dark text)
        BrowseControls.Visibility  = browse ? Visibility.Visible : Visibility.Collapsed;
        BrowseScroll.Visibility    = browse ? Visibility.Visible : Visibility.Collapsed;
        InstalledScroll.Visibility = browse ? Visibility.Collapsed : Visibility.Visible;

        CloseDetailImmediate();

        if (!browse) LoadInstalled();
    }

    // ── Category chips ───────────────────────────────────────────────────

    private void BuildCategoryChips()
    {
        CategoryPanel.Children.Clear();
        AddChip("All", _activeCategory == null, () => { _activeCategory = null; BuildCategoryChips(); _ = SearchAsync(SearchBox.Text.Trim()); });
        foreach (var cat in Categories)
        {
            var c = cat;
            AddChip(Cap(c), _activeCategory == c, () => { _activeCategory = c; BuildCategoryChips(); _ = SearchAsync(SearchBox.Text.Trim()); });
        }
    }

    private void AddChip(string label, bool active, Action onClick)
    {
        var fg = active ? Color.FromRgb(8, 8, 8)      : Color.FromRgb(0x77, 0x77, 0x77);
        var bg = active ? Color.FromRgb(0xD0, 0xD0, 0xD0) : Color.FromRgb(0x18, 0x18, 0x18);
        var bd = active ? Color.FromRgb(0xD0, 0xD0, 0xD0) : Color.FromRgb(0x28, 0x28, 0x28);

        var chip = new Border
        {
            Background      = new SolidColorBrush(bg),
            BorderBrush     = new SolidColorBrush(bd),
            BorderThickness = new Thickness(1),
            CornerRadius    = new CornerRadius(5),
            Padding         = new Thickness(12, 4, 12, 4),
            Margin          = new Thickness(0, 0, 6, 0),
            Cursor          = Cursors.Hand,
            Child           = new TextBlock { Text = label, Foreground = new SolidColorBrush(fg), FontSize = 11 }
        };
        chip.MouseLeftButtonUp += (_, _) => onClick();
        CategoryPanel.Children.Add(chip);
    }

    // ── Search ───────────────────────────────────────────────────────────

    private void SearchBox_KeyDown(object sender, KeyEventArgs e)
    {
        if (e.Key == Key.Enter) _ = SearchAsync(SearchBox.Text.Trim());
    }

    private void Search_Click(object sender, RoutedEventArgs e)
        => _ = SearchAsync(SearchBox.Text.Trim());

    private void OpenFolder_Click(object sender, RoutedEventArgs e) => OpenModsFolder();

    private async Task SearchAsync(string query, bool append = false)
    {
        if (!append)
        {
            _offset = 0;
            _totalHits = 0;
            ModList.Children.Clear();
            StatusText.Text = "Searching...";
        }

        try
        {
            var facets = new List<string> { "\"project_type:mod\"" };
            if (_activeCategory != null) facets.Add($"\"categories:{_activeCategory}\"");

            var facetStr = Uri.EscapeDataString($"[[{string.Join(",", facets)}]]");
            var url = $"https://api.modrinth.com/v2/search?query={Uri.EscapeDataString(query)}&facets={facetStr}&limit=24&offset={_offset}&index=relevance";

            var result = await Http.GetFromJsonAsync<ModSearchResult>(url);
            if (result?.Hits == null || result.Hits.Count == 0)
            {
                if (!append) StatusText.Text = "No mods found.";
                return;
            }

            _totalHits = result.TotalHits;
            _offset += result.Hits.Count;
            StatusText.Text = $"{_totalHits:N0} results";
            foreach (var hit in result.Hits)
                ModList.Children.Add(BuildCard(hit));
        }
        catch (Exception ex)
        {
            StatusText.Text = $"Error: {ex.Message}";
        }
        finally
        {
            _loadingMore = false;
        }
    }

    private void BrowseScroll_ScrollChanged(object sender, ScrollChangedEventArgs e)
    {
        if (_loadingMore || _offset >= _totalHits) return;
        if (BrowseScroll.VerticalOffset >= BrowseScroll.ScrollableHeight - 200)
        {
            _loadingMore = true;
            _ = SearchAsync(SearchBox.Text.Trim(), append: true);
        }
    }

    // ── Browse card ───────────────────────────────────────────────────────

    private UIElement BuildCard(ModHit mod)
    {
        var card = new Border
        {
            Background      = new SolidColorBrush(Color.FromRgb(0x11, 0x11, 0x11)),
            BorderBrush     = new SolidColorBrush(Color.FromRgb(0x22, 0x22, 0x22)),
            BorderThickness = new Thickness(1),
            CornerRadius    = new CornerRadius(10),
            Padding         = new Thickness(14),
            Margin          = new Thickness(0, 0, 0, 8),
            Cursor          = Cursors.Hand
        };
        card.PreviewMouseLeftButtonDown += (_, _) => OpenDetail(mod);

        var grid = new Grid();
        grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
        grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });

        var iconBorder = new Border
        {
            Width = 52, Height = 52, CornerRadius = new CornerRadius(8),
            Background = new SolidColorBrush(Color.FromRgb(0x1A, 0x1A, 0x1A)),
            Margin = new Thickness(0, 0, 14, 0), ClipToBounds = true
        };
        if (!string.IsNullOrEmpty(mod.IconUrl))
        {
            var img = new Image { Stretch = Stretch.UniformToFill };
            _ = LoadImageAsync(img, mod.IconUrl);
            iconBorder.Child = img;
        }
        else
        {
            iconBorder.Child = new TextBlock
            {
                Text = mod.Title.Length > 0 ? mod.Title[0].ToString().ToUpper() : "?",
                Foreground = new SolidColorBrush(Color.FromRgb(0x44, 0x44, 0x44)),
                FontSize = 22, FontWeight = FontWeights.Bold,
                HorizontalAlignment = HorizontalAlignment.Center,
                VerticalAlignment   = VerticalAlignment.Center
            };
        }
        Grid.SetColumn(iconBorder, 0);
        grid.Children.Add(iconBorder);

        var right = new StackPanel { VerticalAlignment = VerticalAlignment.Center };

        var titleRow = new Grid();
        titleRow.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        titleRow.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

        var titleTb = new TextBlock { Text = mod.Title, Foreground = new SolidColorBrush(Color.FromRgb(0xEC, 0xEC, 0xEC)), FontSize = 14, FontWeight = FontWeights.SemiBold };
        Grid.SetColumn(titleTb, 0);
        titleRow.Children.Add(titleTb);

        var dlTb = new TextBlock { Text = FormatNumber(mod.Downloads) + " ↓", Foreground = new SolidColorBrush(Color.FromRgb(0x44, 0x44, 0x44)), FontSize = 11, VerticalAlignment = VerticalAlignment.Center };
        Grid.SetColumn(dlTb, 1);
        titleRow.Children.Add(dlTb);
        right.Children.Add(titleRow);

        right.Children.Add(new TextBlock
        {
            Text = mod.Description, Foreground = new SolidColorBrush(Color.FromRgb(0x55, 0x55, 0x55)),
            FontSize = 12, TextTrimming = TextTrimming.CharacterEllipsis,
            MaxHeight = 36, TextWrapping = TextWrapping.Wrap, Margin = new Thickness(0, 4, 0, 6)
        });

        var chips = new WrapPanel();
        foreach (var cat in (mod.Categories ?? []).Take(4))
            chips.Children.Add(SmallChip(cat));
        right.Children.Add(chips);

        Grid.SetColumn(right, 1);
        grid.Children.Add(right);

        card.Child = grid;
        return card;
    }

    // ── Detail panel ─────────────────────────────────────────────────────

    private void OpenDetail(ModHit mod)
    {
        DetailCol.Width = new GridLength(360);
        _detailOpen = true;
        DetailPanel.Children.Clear();

        var iconBorder = new Border
        {
            Width = 80, Height = 80, CornerRadius = new CornerRadius(12),
            Background = new SolidColorBrush(Color.FromRgb(0x1A, 0x1A, 0x1A)),
            ClipToBounds = true, Margin = new Thickness(0, 0, 0, 14),
            HorizontalAlignment = HorizontalAlignment.Left
        };
        if (!string.IsNullOrEmpty(mod.IconUrl))
        {
            var img = new Image { Stretch = Stretch.UniformToFill };
            _ = LoadImageAsync(img, mod.IconUrl);
            iconBorder.Child = img;
        }
        DetailPanel.Children.Add(iconBorder);

        DetailPanel.Children.Add(Tb(mod.Title, 18, FontWeights.SemiBold, 0xEC, wrap: true));
        DetailPanel.Children.Add(Tb("by " + mod.Author, 12, FontWeights.Normal, 0x55, top: 4, bottom: 2));
        DetailPanel.Children.Add(Tb($"{FormatNumber(mod.Downloads)} downloads  •  {FormatNumber(mod.Follows)} followers", 11, FontWeights.Normal, 0x44, bottom: 14));

        var catWrap = new WrapPanel { Margin = new Thickness(0, 0, 0, 16) };
        foreach (var cat in mod.Categories ?? [])
            catWrap.Children.Add(SmallChip(cat, margin: new Thickness(0, 0, 5, 5)));
        DetailPanel.Children.Add(catWrap);

        DetailPanel.Children.Add(Label("DESCRIPTION"));
        DetailPanel.Children.Add(Tb(mod.Description, 12, FontWeights.Normal, 0x77, bottom: 20, wrap: true));

        DetailPanel.Children.Add(Label("VERSIONS"));
        DetailPanel.Children.Add(Tb(string.Join(", ", (mod.Versions ?? []).TakeLast(8).Reverse()), 12, FontWeights.Normal, 0x55, bottom: 20, wrap: true));

        var installBtn = new Button
        {
            Content = "Install for " + App.Settings.SelectedVersion,
            Style   = (Style)Application.Current.FindResource("PrimaryBtn"),
            Height  = 40, Padding = new Thickness(0),
            HorizontalAlignment = HorizontalAlignment.Stretch
        };
        installBtn.Click += async (_, _) => await InstallModAsync(mod, installBtn);
        DetailPanel.Children.Add(installBtn);

        var folderBtn = new Button
        {
            Content = "Open Mods Folder",
            Style   = (Style)Application.Current.FindResource("GhostBtn"),
            Height  = 36, Padding = new Thickness(0),
            HorizontalAlignment = HorizontalAlignment.Stretch,
            Margin  = new Thickness(0, 8, 0, 0)
        };
        folderBtn.Click += (_, _) => OpenModsFolder();
        DetailPanel.Children.Add(folderBtn);

        SlideDetail(open: true);
    }

    private void CloseDetail_Click(object sender, RoutedEventArgs e) => SlideDetail(open: false);

    private void CloseDetailImmediate()
    {
        if (!_detailOpen) return;
        _detailOpen = false;
        DetailCol.Width = new GridLength(0);
        DetailBorder.RenderTransform = new TranslateTransform(0, 0);
    }

    private void SlideDetail(bool open)
    {
        if (!open && !_detailOpen) return;

        var translate = DetailBorder.RenderTransform as TranslateTransform ?? new TranslateTransform();
        DetailBorder.RenderTransform = translate;

        if (open)
        {
            DetailCol.Width = new GridLength(360);
            _detailOpen = true;
            translate.X = 360;
            var anim = new DoubleAnimation(360, 0, new Duration(TimeSpan.FromMilliseconds(220)))
            {
                EasingFunction = new CubicEase { EasingMode = EasingMode.EaseOut }
            };
            translate.BeginAnimation(TranslateTransform.XProperty, anim);
        }
        else
        {
            _detailOpen = false;
            var anim = new DoubleAnimation(0, 360, new Duration(TimeSpan.FromMilliseconds(180)))
            {
                EasingFunction = new CubicEase { EasingMode = EasingMode.EaseIn }
            };
            anim.Completed += (_, _) => DetailCol.Width = new GridLength(0);
            translate.BeginAnimation(TranslateTransform.XProperty, anim);
        }
    }

    // ── Install ───────────────────────────────────────────────────────────

    private async Task InstallModAsync(ModHit mod, Button btn)
    {
        btn.IsEnabled = false;
        btn.Content   = "Finding version...";
        try
        {
            var mcVer   = Uri.EscapeDataString($"[\"{App.Settings.SelectedVersion}\"]");
            var loaders = Uri.EscapeDataString("[\"fabric\"]");
            var url     = $"https://api.modrinth.com/v2/project/{mod.ProjectId}/version?game_versions={mcVer}&loaders={loaders}";
            var versions = await Http.GetFromJsonAsync<List<ModVersion>>(url);
            var file     = versions?.FirstOrDefault()?.Files?.FirstOrDefault(f => f.Primary)
                        ?? versions?.FirstOrDefault()?.Files?.FirstOrDefault();
            if (file == null) { btn.Content = "No compatible version found"; btn.IsEnabled = true; return; }

            btn.Content = "Downloading...";
            var modsDir = ModsDir();
            Directory.CreateDirectory(modsDir);
            var dest  = Path.Combine(modsDir, file.Filename);
            var bytes = await Http.GetByteArrayAsync(file.Url);
            await File.WriteAllBytesAsync(dest, bytes);
            btn.Content = "Installed ✓";
        }
        catch (Exception ex)
        {
            btn.Content   = $"Failed: {ex.Message}";
            btn.IsEnabled = true;
        }
    }

    // ── Installed mods ────────────────────────────────────────────────────

    private void LoadInstalled()
    {
        InstalledList.Children.Clear();
        StatusText.Text = "";

        var dir     = ModsDir();
        var disDir  = dir + ".disabled";
        Directory.CreateDirectory(dir);
        Directory.CreateDirectory(disDir);

        var active   = Directory.GetFiles(dir,    "*.jar").Select(f => (f, true));
        var disabled = Directory.GetFiles(disDir, "*.jar").Select(f => (f, false));
        var all      = active.Concat(disabled).OrderBy(x => Path.GetFileName(x.f)).ToList();

        if (all.Count == 0)
        {
            InstalledList.Children.Add(Tb("No mods installed.", 13, FontWeights.Normal, 0x44));
            return;
        }

        StatusText.Text = $"{all.Count} mod(s) installed";

        foreach (var (path, enabled) in all)
            InstalledList.Children.Add(BuildInstalledCard(path, enabled));
    }

    private UIElement BuildInstalledCard(string path, bool enabled)
    {
        var name = Path.GetFileNameWithoutExtension(path);
        var size = new FileInfo(path).Length;

        var card = new Border
        {
            Background      = new SolidColorBrush(Color.FromRgb(0x11, 0x11, 0x11)),
            BorderBrush     = new SolidColorBrush(enabled ? Color.FromRgb(0x22, 0x22, 0x22) : Color.FromRgb(0x1A, 0x1A, 0x1A)),
            BorderThickness = new Thickness(1),
            CornerRadius    = new CornerRadius(10),
            Padding         = new Thickness(14, 12, 14, 12),
            Margin          = new Thickness(0, 0, 0, 6),
            Opacity         = enabled ? 1.0 : 0.5
        };

        var grid = new Grid();
        grid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        grid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

        var left = new StackPanel();
        left.Children.Add(new TextBlock
        {
            Text       = name,
            Foreground = new SolidColorBrush(enabled ? Color.FromRgb(0xEC, 0xEC, 0xEC) : Color.FromRgb(0x66, 0x66, 0x66)),
            FontSize   = 13,
            FontWeight = FontWeights.SemiBold,
            TextTrimming = TextTrimming.CharacterEllipsis
        });
        left.Children.Add(new TextBlock
        {
            Text       = $"{FormatSize(size)}  •  {(enabled ? "Active" : "Disabled")}",
            Foreground = new SolidColorBrush(enabled ? Color.FromRgb(0x44, 0x44, 0x44) : Color.FromRgb(0x33, 0x33, 0x33)),
            FontSize   = 11,
            Margin     = new Thickness(0, 3, 0, 0)
        });
        Grid.SetColumn(left, 0);
        grid.Children.Add(left);

        var btns = new StackPanel { Orientation = Orientation.Horizontal, VerticalAlignment = VerticalAlignment.Center };

        var toggleBtn = new Button
        {
            Content         = enabled ? "Disable" : "Enable",
            Style           = (Style)Application.Current.FindResource("GhostBtn"),
            Padding         = new Thickness(12, 5, 12, 5),
            Margin          = new Thickness(0, 0, 6, 0),
            Foreground      = enabled
                ? new SolidColorBrush(Color.FromRgb(0xC8, 0x90, 0x40))
                : new SolidColorBrush(Color.FromRgb(0x5C, 0xB8, 0x5C))
        };
        toggleBtn.Click += (_, _) => { ToggleMod(path, enabled); LoadInstalled(); };

        var deleteBtn = new Button
        {
            Content    = "Delete",
            Style      = (Style)Application.Current.FindResource("GhostBtn"),
            Padding    = new Thickness(12, 5, 12, 5),
            Foreground = new SolidColorBrush(Color.FromRgb(0xC8, 0x50, 0x50))
        };
        deleteBtn.Click += (_, _) =>
        {
            var res = MessageBox.Show($"Delete {name}?", "Wraith Client", MessageBoxButton.YesNo, MessageBoxImage.Question);
            if (res == MessageBoxResult.Yes) { File.Delete(path); LoadInstalled(); }
        };

        btns.Children.Add(toggleBtn);
        btns.Children.Add(deleteBtn);
        Grid.SetColumn(btns, 1);
        grid.Children.Add(btns);

        card.Child = grid;
        return card;
    }

    private static void ToggleMod(string path, bool currentlyEnabled)
    {
        var dir    = ModsDir();
        var disDir = dir + ".disabled";
        Directory.CreateDirectory(disDir);
        var dest = currentlyEnabled
            ? Path.Combine(disDir, Path.GetFileName(path))
            : Path.Combine(dir,    Path.GetFileName(path));
        File.Move(path, dest, overwrite: true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static void OpenModsFolder()
    {
        var dir = ModsDir();
        Directory.CreateDirectory(dir);
        System.Diagnostics.Process.Start("explorer.exe", dir);
    }

    private static string ModsDir() => Path.Combine(
        string.IsNullOrEmpty(App.Settings.GameDirectory)
            ? SettingsService.GetDefaultGameDir()
            : App.Settings.GameDirectory,
        "mods");

    private static async Task LoadImageAsync(Image img, string url)
    {
        try
        {
            var bytes = await Http.GetByteArrayAsync(url);
            var bmp   = new BitmapImage();
            using var ms = new MemoryStream(bytes);
            bmp.BeginInit();
            bmp.CacheOption  = BitmapCacheOption.OnLoad;
            bmp.StreamSource = ms;
            bmp.DecodePixelWidth = 80;
            bmp.EndInit();
            bmp.Freeze();
            img.Dispatcher.Invoke(() => img.Source = bmp);
        }
        catch { }
    }

    private static Border SmallChip(string text, Thickness? margin = null) => new()
    {
        Background      = new SolidColorBrush(Color.FromRgb(0x1A, 0x1A, 0x1A)),
        BorderBrush     = new SolidColorBrush(Color.FromRgb(0x2A, 0x2A, 0x2A)),
        BorderThickness = new Thickness(1),
        CornerRadius    = new CornerRadius(4),
        Padding         = new Thickness(7, 2, 7, 2),
        Margin          = margin ?? new Thickness(0, 0, 4, 0),
        Child           = new TextBlock { Text = Cap(text), Foreground = new SolidColorBrush(Color.FromRgb(0x55, 0x55, 0x55)), FontSize = 10 }
    };

    private static TextBlock Label(string text) => new()
    {
        Text       = text,
        Foreground = new SolidColorBrush(Color.FromRgb(0x44, 0x44, 0x44)),
        FontSize   = 10,
        FontWeight = FontWeights.SemiBold,
        Margin     = new Thickness(0, 0, 0, 6)
    };

    private static TextBlock Tb(string text, int size, FontWeight weight, int hex,
        int top = 0, int bottom = 0, bool wrap = false) => new()
    {
        Text         = text,
        Foreground   = new SolidColorBrush(Color.FromRgb((byte)hex, (byte)hex, (byte)hex)),
        FontSize     = size,
        FontWeight   = weight,
        TextWrapping = wrap ? TextWrapping.Wrap : TextWrapping.NoWrap,
        Margin       = new Thickness(0, top, 0, bottom)
    };

    private static string FormatNumber(long n) => n switch
    {
        >= 1_000_000 => $"{n / 1_000_000.0:0.#}M",
        >= 1_000     => $"{n / 1_000.0:0.#}K",
        _            => n.ToString()
    };

    private static string FormatSize(long bytes) => bytes switch
    {
        >= 1_048_576 => $"{bytes / 1_048_576.0:0.#} MB",
        >= 1_024     => $"{bytes / 1_024.0:0.#} KB",
        _            => $"{bytes} B"
    };

    private static string Cap(string s) =>
        string.IsNullOrEmpty(s) ? s : char.ToUpper(s[0]) + s[1..];
}

// ── Modrinth models ──────────────────────────────────────────────────────

public class ModSearchResult
{
    [JsonPropertyName("hits")]       public List<ModHit>? Hits      { get; set; }
    [JsonPropertyName("total_hits")] public int           TotalHits { get; set; }
}

public class ModHit
{
    [JsonPropertyName("project_id")]  public string        ProjectId   { get; set; } = "";
    [JsonPropertyName("title")]       public string        Title       { get; set; } = "";
    [JsonPropertyName("description")] public string        Description { get; set; } = "";
    [JsonPropertyName("icon_url")]    public string?       IconUrl     { get; set; }
    [JsonPropertyName("downloads")]   public long          Downloads   { get; set; }
    [JsonPropertyName("follows")]     public long          Follows     { get; set; }
    [JsonPropertyName("author")]      public string        Author      { get; set; } = "";
    [JsonPropertyName("categories")]  public List<string>? Categories  { get; set; }
    [JsonPropertyName("versions")]    public List<string>? Versions    { get; set; }
}

public class ModVersion
{
    [JsonPropertyName("files")] public List<ModFile>? Files { get; set; }
}

public class ModFile
{
    [JsonPropertyName("url")]      public string Url      { get; set; } = "";
    [JsonPropertyName("filename")] public string Filename { get; set; } = "";
    [JsonPropertyName("primary")]  public bool   Primary  { get; set; }
}

public class GridLengthAnimation : AnimationTimeline
{
    public GridLength From { get; set; }
    public GridLength To   { get; set; }
    public IEasingFunction? EasingFunction { get; set; }
    public override Type TargetPropertyType => typeof(GridLength);
    protected override Freezable CreateInstanceCore() => new GridLengthAnimation();
    public override object GetCurrentValue(object defaultOriginValue, object defaultDestinationValue, AnimationClock clock)
    {
        if (clock.CurrentProgress == null) return From;
        double t = EasingFunction?.Ease(clock.CurrentProgress.Value) ?? clock.CurrentProgress.Value;
        return new GridLength(From.Value + (To.Value - From.Value) * t);
    }
}
