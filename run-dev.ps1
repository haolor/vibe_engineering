param(
    [string]$FlutterDevice = "chrome"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root "backend"
$frontendDir = Join-Path $root "frontend"

if (-not (Test-Path $backendDir)) {
    throw "Khong tim thay thu muc backend: $backendDir"
}

if (-not (Test-Path $frontendDir)) {
    throw "Khong tim thay thu muc frontend: $frontendDir"
}

Write-Host "[dev] Starting backend (Spring Boot)..."
$backendProcess = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c gradlew.bat bootRun" `
    -WorkingDirectory $backendDir `
    -PassThru

Write-Host "[dev] Backend PID: $($backendProcess.Id)"
Write-Host "[dev] Starting frontend (Flutter) on device: $FlutterDevice"

try {
    Push-Location $frontendDir
    flutter run -d $FlutterDevice
}
finally {
    Pop-Location
    if ($backendProcess -and -not $backendProcess.HasExited) {
        Write-Host "[dev] Stopping backend..."
        Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue
    }
}
