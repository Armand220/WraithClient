using System.Text;

namespace WraithClient.Services;

public record AuthResult
{
    public bool    Success     { get; init; }
    public string? AccessToken { get; init; }
    public string? Username    { get; init; }
    public string? Uuid        { get; init; }
    public string? Error       { get; init; }
}

public class AuthService
{
    // ── Offline / cracked ─────────────────────────────────────────────────

    public static AuthResult GetOfflineSession(string username) => new()
    {
        Success     = true,
        Username    = username.Length > 0 ? username : "Player",
        Uuid        = OfflineUuid(username),
        AccessToken = "offline"
    };

    private static string OfflineUuid(string name)
    {
        var hash = System.Security.Cryptography.MD5.HashData(
            Encoding.UTF8.GetBytes($"OfflinePlayer:{name}"));
        hash[6] = (byte)((hash[6] & 0x0f) | 0x30);
        hash[8] = (byte)((hash[8] & 0x3f) | 0x80);
        return new Guid(hash).ToString("N");
    }
}

public static class AuthResultExtensions
{
    public static bool IsOffline(this AuthResult r) => r.AccessToken == "offline";
}
