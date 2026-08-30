using DiscordRPC;
using DiscordRPC.Logging;

namespace WraithClient.Services;

public class DiscordRPCService : IDisposable
{
    private const string AppId = "1542977382426742784";

    private DiscordRpcClient? _client;
    private System.Threading.Timer? _invokeTimer;
    private bool _running;
    private bool _playing;
    private string _version = "";
    private string _server  = "";
    private DateTime _startTime = DateTime.UtcNow;

    public void Start()
    {
        if (_running) return;

        _client = new DiscordRpcClient(AppId);
        _client.Logger = new ConsoleLogger { Level = LogLevel.Warning };

        _client.OnReady += (_, e) => { };
        _client.OnError += (_, e) => { };

        _client.Initialize();
        _running = true;

        // Invoke must be called repeatedly to process pipe messages and fire events.
        _invokeTimer = new System.Threading.Timer(_ =>
        {
            try { _client?.Invoke(); } catch { }
        }, null, TimeSpan.Zero, TimeSpan.FromMilliseconds(500));

        SetIdle();
    }

    public void SetIdle()
    {
        if (!_running || _client == null) return;
        _playing = false;

        _client.SetPresence(new RichPresence
        {
            Details = "Playing Wraith Client",
            State   = $"Version {App.Settings.SelectedVersion}",
            Assets  = new Assets
            {
                LargeImageKey  = "wraithlogorpc",
                LargeImageText = "Wraith Client"
            },
            Timestamps = new Timestamps(DateTime.UtcNow)
        });
    }

    public void SetPlaying(string version, string? serverIp = null)
    {
        if (!_running || _client == null) return;

        _playing   = true;
        _version   = version;
        _server    = serverIp ?? "";
        _startTime = DateTime.UtcNow;

        _client.SetPresence(new RichPresence
        {
            Details = "Playing Wraith Client",
            State   = $"Version {version}",
            Assets  = new Assets
            {
                LargeImageKey  = "wraithlogorpc",
                LargeImageText = "Wraith Client"
            },
            Timestamps = new Timestamps(_startTime)
        });
    }

    public void UpdateServer(string serverIp)
    {
        if (!_running || _client == null) return;
        _server = serverIp;

        _client.SetPresence(new RichPresence
        {
            Details = "Playing Wraith Client",
            State   = $"Version {_version}",
            Assets  = new Assets
            {
                LargeImageKey  = "wraithlogorpc",
                LargeImageText = "Wraith Client"
            },
            Timestamps = new Timestamps(_startTime)
        });
    }

    public void Stop()
    {
        if (!_running) return;
        _running = false;
        _playing = false;
        _invokeTimer?.Dispose();
        _invokeTimer = null;
        try { _client?.ClearPresence(); _client?.Invoke(); } catch { }
        _client?.Dispose();
        _client = null;
    }

    public void Dispose()
    {
        Stop();
    }
}
