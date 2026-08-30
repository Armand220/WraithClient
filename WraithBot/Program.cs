using System.Security.Cryptography;
using System.Text;
using Discord;
using Discord.WebSocket;

// Load config
var configPath = Path.Combine(AppContext.BaseDirectory, "bot_config.json");
if (!File.Exists(configPath))
{
    Console.WriteLine("bot_config.json not found. Creating template...");
    File.WriteAllText(configPath, """
    {
        "token_encrypted": "RUN_Encrypt-WraithToken.ps1_TO_GENERATE_THIS",
        "shared_secret": "CHANGE_THIS_TO_A_LONG_RANDOM_STRING",
        "allowed_guild_id": 0,
        "allowed_channel_id": 0
    }
    """);
    Console.WriteLine("Run Encrypt-WraithToken.ps1 to encrypt your token, then paste the output into bot_config.json.");
    return;
}

var cfg = System.Text.Json.JsonSerializer.Deserialize<Config>(File.ReadAllText(configPath))!;

// Decrypt token using Windows DPAPI
string token;
try
{
    var encrypted  = Convert.FromBase64String(cfg.TokenEncrypted);
    var plainBytes = ProtectedData.Unprotect(encrypted, null, DataProtectionScope.CurrentUser);
    token = Encoding.UTF8.GetString(plainBytes);
}
catch
{
    Console.WriteLine("Failed to decrypt token. Re-run Encrypt-WraithToken.ps1 on this machine/user account.");
    return;
}

var client = new DiscordSocketClient(new DiscordSocketConfig
{
    GatewayIntents = GatewayIntents.Guilds
});

client.Log += msg => { Console.WriteLine(msg.ToString()); return Task.CompletedTask; };

client.Ready += async () =>
{
    Console.WriteLine($"Logged in as {client.CurrentUser.Username}");

    var cmd = new SlashCommandBuilder()
        .WithName("newkey")
        .WithDescription("Generate a new Wraith Client moderator key")
        .WithDefaultMemberPermissions(GuildPermission.Administrator)
        .Build();

    if (cfg.AllowedGuildId != 0)
        await client.Rest.CreateGuildCommand(cmd, cfg.AllowedGuildId);
    else
        await client.Rest.CreateGlobalCommand(cmd);
};

client.SlashCommandExecuted += async interaction =>
{
    if (interaction.CommandName != "newkey") return;

    if (interaction.User.Id != cfg.OwnerUserId)
    {
        await interaction.RespondAsync("You are not authorised to use this command.", ephemeral: true);
        Console.WriteLine($"[{DateTime.UtcNow:u}] Blocked unauthorised attempt by {interaction.User.Username} ({interaction.User.Id})");
        return;
    }

    var key = GenerateKey(cfg.SharedSecret);
    await interaction.RespondAsync(
        $"**New Wraith Moderator Key**\n```\n{key}\n```\nPaste this into the Moderator tab of the launcher. Expires in 30 days.",
        ephemeral: true);

    Console.WriteLine($"[{DateTime.UtcNow:u}] Key issued to {interaction.User.Username}");
};

await client.LoginAsync(TokenType.Bot, token);
await client.StartAsync();
await Task.Delay(Timeout.Infinite);

static string GenerateKey(string secret)
{
    var nonce   = Convert.ToBase64String(RandomNumberGenerator.GetBytes(12))
                         .Replace("+", "").Replace("/", "").Replace("=", "")[..12];
    var ts      = DateTimeOffset.UtcNow.ToUnixTimeSeconds().ToString();
    var payload = $"{ts}:{nonce}";
    var hmac    = ComputeHmac(secret, payload);
    return $"wraith_{payload}_{hmac[..16]}";
}

static string ComputeHmac(string secret, string data)
{
    var key = Encoding.UTF8.GetBytes(secret);
    var msg = Encoding.UTF8.GetBytes(data);
    return Convert.ToHexString(HMACSHA256.HashData(key, msg)).ToLower();
}

record Config(
    [property: System.Text.Json.Serialization.JsonPropertyName("token_encrypted")]    string TokenEncrypted,
    [property: System.Text.Json.Serialization.JsonPropertyName("shared_secret")]      string SharedSecret,
    [property: System.Text.Json.Serialization.JsonPropertyName("owner_user_id")]      ulong  OwnerUserId,
    [property: System.Text.Json.Serialization.JsonPropertyName("allowed_guild_id")]   ulong  AllowedGuildId,
    [property: System.Text.Json.Serialization.JsonPropertyName("allowed_channel_id")] ulong  AllowedChannelId
);
