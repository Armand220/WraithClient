using System.IO;
using System.IO.Compression;
using System.Text.Json;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Media;
using WraithClient.Services;

namespace WraithClient.Views;

public partial class CheatsView : Page
{
    private record CheatJar(string Id, string Name, string Version, string McVersion, string FileName);
    private record CheatGroup(string Id, string Name, List<CheatJar> Jars);

    public static string CheatsDir =>
        Path.Combine(AppContext.BaseDirectory, "cheats");

    private List<CheatGroup> _groups = new();

    public CheatsView()
    {
        InitializeComponent();
        Loaded += (_, _) => Refresh();
    }

    private void Refresh()
    {
        _groups = LoadGroups();
        CheatList.Children.Clear();

        if (_groups.Count == 0)
        {
            CheatList.Children.Add(new TextBlock
            {
                Text       = "No cheats found in the cheats folder.",
                Foreground = Brush(0x44, 0x44, 0x44),
                FontSize   = 13,
                Margin     = new Thickness(0, 20, 0, 0)
            });
            return;
        }

        foreach (var group in _groups)
            CheatList.Children.Add(BuildCard(group));

        UpdateConflictBanner();
    }

    // ── Card builder ──────────────────────────────────────────────────────

    private UIElement BuildCard(CheatGroup group)
    {
        bool    enabled  = App.Settings.EnabledCheats.ContainsKey(group.Id);
        string? selFile  = App.Settings.EnabledCheats.GetValueOrDefault(group.Id);
        var     selJar   = group.Jars.FirstOrDefault(j => j.FileName == selFile) ?? group.Jars[0];
        bool    conflict = HasVersionConflict(group.Id, selJar.McVersion);
        bool    multi    = group.Jars.Count > 1;

        var card = new Border
        {
            Background      = Brush(0x19, 0x19, 0x19),
            CornerRadius    = new CornerRadius(8),
            Margin          = new Thickness(0, 0, 0, 10),
            Padding         = new Thickness(16, 14, 16, 14),
            BorderThickness = new Thickness(1),
            BorderBrush     = conflict ? BrushA(0xAA, 0xFF, 0x55, 0x55) : Brush(0x26, 0x26, 0x26)
        };

        var stack = new StackPanel();

        // ── Row 1: name left, toggle right ───────────────────────────────
        var row1 = new Grid();
        row1.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        row1.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

        var nameStack = new StackPanel { VerticalAlignment = VerticalAlignment.Center };
        nameStack.Children.Add(new TextBlock
        {
            Text       = group.Name,
            Foreground = Brush(0xCC, 0xCC, 0xCC),
            FontSize   = 14,
            FontWeight = FontWeights.SemiBold
        });
        nameStack.Children.Add(new TextBlock
        {
            Text       = multi ? $"{group.Jars.Count} versions  ·  MC {group.Jars.Select(j => j.McVersion).Distinct().Count()} targets"
                               : $"v{selJar.Version}  ·  MC {selJar.McVersion}",
            Foreground = Brush(0x44, 0x44, 0x44),
            FontSize   = 11,
            Margin     = new Thickness(0, 3, 0, 0)
        });
        if (conflict)
            nameStack.Children.Add(new TextBlock
            {
                Text       = "⚠  Version not compatible with other enabled cheats",
                Foreground = Brush(0xFF, 0x55, 0x55),
                FontSize   = 11,
                Margin     = new Thickness(0, 4, 0, 0)
            });

        Grid.SetColumn(nameStack, 0);
        row1.Children.Add(nameStack);

        var toggle = MakeToggle(group.Id, enabled);
        toggle.Tag    = group;
        toggle.Click += Toggle_Click;
        Grid.SetColumn(toggle, 1);
        row1.Children.Add(toggle);

        stack.Children.Add(row1);

        // ── Row 2: custom version picker (multi-version only) ─────────────
        if (multi)
        {
            var sorted = SortedJars(group.Jars);
            stack.Children.Add(BuildVersionPicker(group.Id, sorted, selJar));
        }

        card.Child = stack;
        return card;
    }

    // ── Custom version picker ─────────────────────────────────────────────

    private UIElement BuildVersionPicker(string groupId, List<CheatJar> sorted, CheatJar selected)
    {
        var container = new Border { Margin = new Thickness(0, 12, 0, 0) };

        // The "selector button" shows the currently picked version
        var selectorBtn = new Border
        {
            Background      = Brush(0x10, 0x10, 0x10),
            BorderBrush     = Brush(0x30, 0x30, 0x30),
            BorderThickness = new Thickness(1),
            CornerRadius    = new CornerRadius(6),
            Padding         = new Thickness(12, 0, 10, 0),
            Height          = 34,
            Cursor          = Cursors.Hand
        };

        var selectorRow = new Grid();
        selectorRow.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        selectorRow.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

        var selectorLabel = new TextBlock
        {
            Text              = $"MC {selected.McVersion}   —   v{selected.Version}",
            Foreground        = Brush(0xAA, 0xAA, 0xAA),
            FontSize          = 12,
            VerticalAlignment = VerticalAlignment.Center
        };
        Grid.SetColumn(selectorLabel, 0);
        selectorRow.Children.Add(selectorLabel);

        var arrow = new TextBlock
        {
            Text              = "▾",
            Foreground        = Brush(0x55, 0x55, 0x55),
            FontSize          = 11,
            VerticalAlignment = VerticalAlignment.Center
        };
        Grid.SetColumn(arrow, 1);
        selectorRow.Children.Add(arrow);

        selectorBtn.Child = selectorRow;

        // Popup dropdown list
        var popup = new Popup
        {
            Placement       = PlacementMode.Bottom,
            PlacementTarget = selectorBtn,
            StaysOpen       = false,
            AllowsTransparency = true,
            PopupAnimation  = PopupAnimation.Fade
        };

        var dropBorder = new Border
        {
            Background      = Brush(0x14, 0x14, 0x14),
            BorderBrush     = Brush(0x33, 0x33, 0x33),
            BorderThickness = new Thickness(1),
            CornerRadius    = new CornerRadius(6),
            Padding         = new Thickness(0, 4, 0, 4),
            Effect          = new System.Windows.Media.Effects.DropShadowEffect
                { BlurRadius = 16, Opacity = 0.5, ShadowDepth = 4, Color = Colors.Black }
        };

        var dropList = new StackPanel();

        foreach (var jar in sorted)
        {
            bool isSel = jar.FileName == selected.FileName;

            var row = new Border
            {
                Padding         = new Thickness(14, 7, 14, 7),
                Background      = isSel ? Brush(0x22, 0x22, 0x22) : Brushes.Transparent,
                Cursor          = Cursors.Hand,
                Tag             = (groupId, jar, selectorLabel, popup)
            };

            var rowInner = new Grid();
            rowInner.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
            rowInner.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });

            rowInner.Children.Add(new TextBlock
            {
                Text              = $"MC {jar.McVersion}",
                Foreground        = isSel ? Brush(0xEE, 0xEE, 0xEE) : Brush(0x88, 0x88, 0x88),
                FontSize          = 12,
                VerticalAlignment = VerticalAlignment.Center
            });

            var verTag = new Border
            {
                Background    = Brush(0x1E, 0x1E, 0x1E),
                CornerRadius  = new CornerRadius(3),
                Padding       = new Thickness(6, 2, 6, 2),
                VerticalAlignment = VerticalAlignment.Center
            };
            verTag.Child = new TextBlock
            {
                Text       = "v" + jar.Version,
                Foreground = Brush(0x55, 0x55, 0x55),
                FontSize   = 11
            };
            Grid.SetColumn(verTag, 1);
            rowInner.Children.Add(verTag);

            row.Child = rowInner;

            // Hover effect
            row.MouseEnter += (s, _) => { if (s is Border b && !isSel) b.Background = Brush(0x1C, 0x1C, 0x1C); };
            row.MouseLeave += (s, _) => { if (s is Border b && !isSel) b.Background = Brushes.Transparent; };

            row.MouseLeftButtonUp += VersionRow_Click;
            dropList.Children.Add(row);
        }

        dropBorder.Child   = dropList;
        popup.Child        = dropBorder;

        // Bind popup width to selector button width
        selectorBtn.Loaded += (_, _) =>
        {
            popup.Width = selectorBtn.ActualWidth;
        };
        selectorBtn.SizeChanged += (_, e) => popup.Width = e.NewSize.Width;

        selectorBtn.MouseLeftButtonUp += (_, _) =>
        {
            popup.IsOpen = !popup.IsOpen;
            arrow.Text   = popup.IsOpen ? "▴" : "▾";
        };
        popup.Closed += (_, _) => arrow.Text = "▾";

        container.Child = selectorBtn;

        // Store popup ref on container so we can close it
        container.Tag = popup;
        return container;
    }

    private void VersionRow_Click(object sender, MouseButtonEventArgs e)
    {
        if (sender is not Border row) return;
        if (row.Tag is not (string groupId, CheatJar jar, TextBlock label, Popup popup)) return;

        // Save selection (preserve enabled state)
        App.Settings.EnabledCheats[groupId] = jar.FileName;
        SettingsService.Save(App.Settings);

        popup.IsOpen = false;
        Refresh();
    }

    private void Toggle_Click(object sender, RoutedEventArgs e)
    {
        if (sender is not Button btn || btn.Tag is not CheatGroup group) return;

        if (App.Settings.EnabledCheats.ContainsKey(group.Id))
        {
            App.Settings.EnabledCheats.Remove(group.Id);
        }
        else
        {
            string? saved  = App.Settings.EnabledCheats.GetValueOrDefault(group.Id);
            var     jar    = group.Jars.FirstOrDefault(j => j.FileName == saved) ?? SortedJars(group.Jars)[0];
            App.Settings.EnabledCheats[group.Id] = jar.FileName;
        }

        SettingsService.Save(App.Settings);
        Refresh();
    }

    // ── Conflict check ────────────────────────────────────────────────────

    private bool HasVersionConflict(string id, string mcVersion)
    {
        if (!App.Settings.EnabledCheats.ContainsKey(id)) return false;
        return _groups
            .Where(g => g.Id != id && App.Settings.EnabledCheats.ContainsKey(g.Id))
            .Any(g => {
                var fn  = App.Settings.EnabledCheats[g.Id];
                var jar = g.Jars.FirstOrDefault(j => j.FileName == fn) ?? g.Jars[0];
                return jar.McVersion != mcVersion;
            });
    }

    private void UpdateConflictBanner()
    {
        bool any = _groups.Any(g => {
            var fn  = App.Settings.EnabledCheats.GetValueOrDefault(g.Id);
            var jar = g.Jars.FirstOrDefault(j => j.FileName == fn) ?? g.Jars[0];
            return HasVersionConflict(g.Id, jar.McVersion);
        });
        ConflictBanner.Visibility = any ? Visibility.Visible : Visibility.Collapsed;
    }

    // ── Load & group ──────────────────────────────────────────────────────

    private static List<CheatGroup> LoadGroups()
    {
        var dir = CheatsDir;
        if (!Directory.Exists(dir)) return new();

        return Directory.EnumerateFiles(dir, "*.jar")
            .Select(ParseCheatJar).OfType<CheatJar>()
            .GroupBy(j => j.Id)
            .Select(g => new CheatGroup(g.Key, g.First().Name, g.ToList()))
            .OrderBy(g => g.Name)
            .ToList();
    }

    private static CheatJar? ParseCheatJar(string path)
    {
        try
        {
            using var zip  = ZipFile.OpenRead(path);
            var meta = zip.GetEntry("fabric.mod.json");
            if (meta == null) return null;
            using var doc  = JsonDocument.Parse(new StreamReader(meta.Open()).ReadToEnd());
            var root = doc.RootElement;
            string id  = root.TryGetProperty("id",      out var p) ? p.GetString() ?? "" : "";
            string nm  = root.TryGetProperty("name",    out var n) ? n.GetString() ?? id : id;
            string ver = root.TryGetProperty("version", out var v) ? v.GetString() ?? "?" : "?";
            string mc  = "?";
            if (root.TryGetProperty("depends", out var d) && d.TryGetProperty("minecraft", out var m))
            {
                if (m.ValueKind == JsonValueKind.Array)
                    mc = m.EnumerateArray().Select(e => e.GetString() ?? "").FirstOrDefault(s => s.Length > 0) ?? "?";
                else
                    mc = ParseMcRange(m.GetString() ?? "");
            }
            return new CheatJar(id, nm, ver, mc, Path.GetFileName(path));
        }
        catch { return null; }
    }

    private static List<CheatJar> SortedJars(List<CheatJar> jars) =>
        jars.OrderByDescending(j => j.McVersion, StringComparer.Ordinal)
            .ThenByDescending(j => { var d = j.Version.LastIndexOf('-'); return d >= 0 && int.TryParse(j.Version[(d+1)..], out var n) ? n : 0; })
            .ToList();

    // ── Helpers ───────────────────────────────────────────────────────────

    private static Button MakeToggle(string id, bool enabled) => new()
    {
        Content         = enabled ? "Enabled" : "Disabled",
        Width           = 90, Height = 32, FontSize = 12,
        Foreground      = enabled ? Brush(0x81, 0xC7, 0x84) : Brush(0x48, 0x48, 0x48),
        Background      = enabled ? Brush(0x1B, 0x5E, 0x20) : Brush(0x16, 0x16, 0x16),
        BorderBrush     = BrushA(0x40, 0xFF, 0xFF, 0xFF),
        BorderThickness = new Thickness(1),
        Cursor          = Cursors.Hand,
        VerticalAlignment = VerticalAlignment.Center
    };

    private static SolidColorBrush Brush(byte r, byte g, byte b) =>
        new(Color.FromRgb(r, g, b));
    private static SolidColorBrush BrushA(byte a, byte r, byte g, byte b) =>
        new(Color.FromArgb(a, r, g, b));

    // Parses minecraft dep strings like ">=1.20 <=1.20.1" or "~1.21.4" into "1.20 – 1.20.1"
    private static string ParseMcRange(string raw)
    {
        if (string.IsNullOrWhiteSpace(raw)) return "?";
        var versions = System.Text.RegularExpressions.Regex
            .Matches(raw, @"\d+\.\d+[\.\d]*")
            .Select(m => m.Value)
            .Distinct()
            .ToList();
        return versions.Count switch
        {
            0 => raw.TrimStart('~', '^', '>', '<', '=', ' '),
            1 => versions[0],
            _ => $"{versions[0]} – {versions[^1]}"
        };
    }

    // Called by HomeView
    public static string? GetIdFromFile(string path) => ParseCheatJar(path)?.Id;
}
