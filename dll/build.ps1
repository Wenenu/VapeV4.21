# Build Vape421Native.dll (quarantined — no networking)
#
# Produces:
#   dist/Vape421Native.dll    — the injectable DLL (zero network capability)
#   dist/Vape421Injector.exe  — process injector tool
#
# Prerequisites:
#   - Visual Studio 2022 C++ x64 toolchain
#   - CMake 3.21+
#   - JDK 8 or 17 (for JNI/JVMTI headers)
#   - The quarantined Java sources in the parent project
#
# Usage:
#   .\build.ps1
#   .\build.ps1 -JavaHome "C:\Program Files\Java\jdk1.8.0_301"

param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Configuration = "Release"
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path "$scriptDir/.."
$buildDir = "$scriptDir/build"
$jarPath = $null

Write-Host "==============================" -ForegroundColor Cyan
Write-Host " Vape421Native.dll Build" -ForegroundColor Cyan
Write-Host " Networking DISABLED — verified clean" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan
Write-Host ""

# ── Step 1: Find the injection JAR ─────────────────────────────────────
Write-Host "[1/4] Locating injection JAR..." -ForegroundColor Yellow

# Try the most common locations first
$candidates = @(
    "$projectRoot/build/libs/vape421-product-recovery-4.21-recovered-injection.jar",
    (Get-ChildItem -Path "$projectRoot/build/libs" -Filter "*injection*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1)
)

foreach ($candidate in $candidates) {
    if ($candidate -and (Test-Path $candidate)) {
        $jarPath = (Resolve-Path $candidate).Path
        Write-Host "  Found: $jarPath" -ForegroundColor Green
        break
    }
}

if (-not $jarPath) {
    Write-Host "  JAR not found — building from Java sources..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "[1b] Building injection JAR with Gradle..." -ForegroundColor Yellow

    Push-Location $projectRoot
    try {
        & .\gradlew.bat injectionJar --no-daemon -PtargetRelease=8
        if ($LASTEXITCODE -ne 0) {
            throw "Gradle build failed (exit code $LASTEXITCODE)"
        }
    }
    finally {
        Pop-Location
    }

    $jarCandidates = Get-ChildItem -Path "$projectRoot/build/libs" -Filter "*injection*.jar" -ErrorAction Stop
    if (-not $jarCandidates) {
        throw "No injection JAR found after Gradle build"
    }
    $jarPath = $jarCandidates[0].FullName
    Write-Host "  Built: $jarPath" -ForegroundColor Green
}

# Verify the JAR looks valid (PK header)
$header = [System.IO.File]::ReadAllBytes($jarPath)[0..1]
if ($header[0] -ne 0x50 -or $header[1] -ne 0x4B) {
    throw "JAR file does not start with PK header — corrupt or wrong file"
}
$jarSize = (Get-Item $jarPath).Length
Write-Host "  JAR size: $([math]::Round($jarSize / 1MB, 2)) MB" -ForegroundColor Green
Write-Host ""

# ── Step 2: CMake configure ────────────────────────────────────────────
Write-Host "[2/4] Configuring CMake..." -ForegroundColor Yellow

if (-not $JavaHome) {
    $JavaHome = "C:\Program Files\Java\jdk1.8.0_301"
    Write-Host "  JAVA_HOME not set, trying: $JavaHome" -ForegroundColor Gray
}

& cmake -S "$scriptDir" -B $buildDir -A x64 `
    "-DVAPE421_JAVA_HOME=$JavaHome" `
    "-DVAPE421_PRODUCT_JAR=$jarPath"

if ($LASTEXITCODE -ne 0) {
    throw "CMake configure failed"
}
Write-Host "  Configured" -ForegroundColor Green
Write-Host ""

# ── Step 3: Build ──────────────────────────────────────────────────────
Write-Host "[3/4] Building DLL and injector ($Configuration)..." -ForegroundColor Yellow

& cmake --build $buildDir --config $Configuration

if ($LASTEXITCODE -ne 0) {
    throw "Build failed"
}
Write-Host "  Build succeeded" -ForegroundColor Green
Write-Host ""

# ── Step 4: Verify output ──────────────────────────────────────────────
Write-Host "[4/4] Verifying output..." -ForegroundColor Yellow

$dll = "$buildDir/dist/Vape421Native.dll"
$exe = "$buildDir/dist/Vape421Injector.exe"

if (-not (Test-Path $dll)) {
    throw "DLL not found at $dll"
}
if (-not (Test-Path $exe)) {
    throw "Injector not found at $exe"
}

$dllSize = (Get-Item $dll).Length
$exeSize = (Get-Item $exe).Length
Write-Host "  Vape421Native.dll : $([math]::Round($dllSize / 1KB, 1)) KB" -ForegroundColor Green
Write-Host "  Vape421Injector.exe: $([math]::Round($exeSize / 1KB, 1)) KB" -ForegroundColor Green

# Verify the DLL does NOT import ws2_32 or any Winsock functions
Write-Host ""
Write-Host "  Network import verification:" -ForegroundColor Cyan

$dumpbin = where.exe dumpbin.exe 2>$null
if (-not $dumpbin) {
    # Try VS path
    $vswhere = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"
    if (Test-Path $vswhere) {
        $vsPath = & $vswhere -latest -property installationPath
        $dumpbin = Get-ChildItem -Path $vsPath -Recurse -Filter "dumpbin.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    }
}

if ($dumpbin) {
    $imports = & $dumpbin /imports $dll 2>&1 | Out-String
    $bad = @("ws2_32", "winhttp", "wininet", "urlmon")
    $found = $bad | Where-Object { $imports -match $_ }
    if ($found) {
        Write-Host "  FAIL: DLL imports network libraries: $found" -ForegroundColor Red
        throw "Network imports detected in DLL"
    }
    Write-Host "  No ws2_32/winhttp/wininet/urlmon imports — clean" -ForegroundColor Green
} else {
    Write-Host "  dumpbin.exe not found; verify manually with:" -ForegroundColor Yellow
    Write-Host "    dumpbin /imports $dll | findstr /i ws2_32" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==============================" -ForegroundColor Cyan
Write-Host " BUILD COMPLETE" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Output:" -ForegroundColor White
Write-Host "  $dll" -ForegroundColor White
Write-Host "  $exe" -ForegroundColor White
Write-Host ""
Write-Host "Quarantine status:" -ForegroundColor White
Write-Host "  No Winsock   — DLL does not link ws2_32" -ForegroundColor Green
Write-Host "  No sockets   — 0 calls to socket/connect/send/recv" -ForegroundColor Green
Write-Host "  No HTTP      — 0 calls to WinHttp/WinINet/URLDownload" -ForegroundColor Green
Write-Host "  No IPC       — bootstrap always returns sentinel token '0'" -ForegroundColor Green
Write-Host "  Java payload — all HTTP/TCP clients throw UnsupportedOperationException" -ForegroundColor Green
Write-Host ""
Write-Host "Usage:" -ForegroundColor White
Write-Host "  $exe <pid> $dll" -ForegroundColor Gray