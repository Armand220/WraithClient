using WraithClient.Models;

namespace WraithClient.Services;

public class VersionManager
{
    // Curated list of all supported versions, newest first.
    // Add new versions here as Mojang releases them.
    private static readonly string[] SupportedVersions =
    {
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
    };

    public Task<List<MCVersion>> GetVersionsAsync(
        bool includeSnapshots = false,
        bool includeOldVersions = false,
        CancellationToken ct = default)
    {
        var list = SupportedVersions
            .Select(id => new MCVersion { Id = id, Type = "release" })
            .ToList();
        return Task.FromResult(list);
    }

    public List<MCVersion> GetCached() =>
        SupportedVersions
            .Select(id => new MCVersion { Id = id, Type = "release" })
            .ToList();
}
