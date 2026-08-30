Add-Type -AssemblyName System.Security

Write-Host ""
Write-Host "  Wraith Bot - Token Encryptor" -ForegroundColor Cyan
Write-Host "  ==============================" -ForegroundColor Cyan
Write-Host "  Your token is encrypted using Windows DPAPI." -ForegroundColor Gray
Write-Host "  It can only be decrypted by YOU on THIS machine." -ForegroundColor Gray
Write-Host ""

$secure = Read-Host "Paste your Discord bot token" -AsSecureString
$bstr   = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
$plain  = [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)

if ([string]::IsNullOrWhiteSpace($plain)) {
    Write-Host "No token entered. Exiting." -ForegroundColor Red
    exit 1
}

$bytes     = [System.Text.Encoding]::UTF8.GetBytes($plain)
$encrypted = [System.Security.Cryptography.ProtectedData]::Protect(
                $bytes, $null,
                [System.Security.Cryptography.DataProtectionScope]::CurrentUser)
$base64    = [Convert]::ToBase64String($encrypted)

Write-Host ""
Write-Host "  Encrypted token (copy this into bot_config.json as token_encrypted):" -ForegroundColor Green
Write-Host ""
Write-Host $base64 -ForegroundColor White
Write-Host ""

# Optionally write directly into bot_config.json if it exists
$configPath = Join-Path $PSScriptRoot "bot_config.json"
if (Test-Path $configPath) {
    $cfg = Get-Content $configPath -Raw | ConvertFrom-Json
    $cfg.token_encrypted = $base64
    $cfg | ConvertTo-Json -Depth 5 | Set-Content $configPath -Encoding UTF8
    Write-Host "  bot_config.json updated automatically." -ForegroundColor Green
} else {
    Write-Host "  bot_config.json not found - paste the value above manually." -ForegroundColor Yellow
}

Write-Host ""
