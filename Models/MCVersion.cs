namespace WraithClient.Models;

public class MCVersion
{
    public string Id { get; set; } = string.Empty;
    public string Type { get; set; } = string.Empty;
    public string Url { get; set; } = string.Empty;
    public DateTime ReleaseTime { get; set; }
    public bool IsInstalled { get; set; }

    public string DisplayName => Type == "release" ? Id : $"{Id} ({Type})";
    public override string ToString() => DisplayName;
    public string TypeLabel => Type switch
    {
        "release" => "Release",
        "snapshot" => "Snapshot",
        "old_beta" => "Beta",
        "old_alpha" => "Alpha",
        _ => Type
    };
}

public class VersionManifest
{
    public VersionManifestLatest? latest { get; set; }
    public List<MCVersion>? versions { get; set; }
}

public class VersionManifestLatest
{
    public string? release { get; set; }
    public string? snapshot { get; set; }
}
