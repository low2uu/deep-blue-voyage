param(
    [ValidateSet('prepare', 'build', 'run', 'debug')]
    [string]$Action = 'build'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jdkPath = $env:JAVA_HOME
$localSettings = Join-Path $projectRoot '.vscode/settings.json'
if (Test-Path -LiteralPath $localSettings) {
    $settings = Get-Content -LiteralPath $localSettings -Raw | ConvertFrom-Json
    if ($settings.'java.jdt.ls.java.home') { $jdkPath = $settings.'java.jdt.ls.java.home' }
}
if (!$jdkPath -or !(Test-Path -LiteralPath (Join-Path $jdkPath 'bin/javac.exe'))) {
    throw 'Configure a Java 21 JDK in JAVA_HOME or .vscode/settings.json.'
}
$env:JAVA_HOME = $jdkPath
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.local/gradle-home'
$session = Get-Content (Join-Path $projectRoot 'config/development-session.json') -Raw | ConvertFrom-Json
if ($session.constructionMode -isnot [bool]) { throw 'constructionMode must be a JSON boolean.' }
foreach ($name in @('renderDistance', 'simulationDistance', 'maxFps')) {
    $value = $session.$name
    $maximum = if ($name -eq 'maxFps') { 260 } else { 32 }
    $minimum = if ($name -eq 'maxFps') { 10 } elseif ($name -eq 'simulationDistance') { 5 } else { 2 }
    if ($null -eq $value -or $value -is [string] -or [double]$value -ne [int]$value -or $value -lt $minimum -or $value -gt $maximum) {
        throw "$name must be an integer from $minimum to $maximum."
    }
}
$construction = [bool]$session.constructionMode
if ($Action -in @('run', 'debug')) {
    $clientDir = Join-Path $projectRoot 'runtime/client'
    New-Item -ItemType Directory -Force -Path $clientDir | Out-Null
    $optionsPath = Join-Path $clientDir 'options.txt'
    $backupPath = Join-Path $clientDir 'options.before-construction.txt'
    if ($construction) {
        if (!(Test-Path -LiteralPath $backupPath)) {
            if (Test-Path -LiteralPath $optionsPath) { Copy-Item -LiteralPath $optionsPath -Destination $backupPath }
            else { [IO.File]::WriteAllText($backupPath, '') }
        }
        $lines = if (Test-Path -LiteralPath $optionsPath) { @(Get-Content -LiteralPath $optionsPath) } else { @() }
        foreach ($name in @('renderDistance', 'simulationDistance', 'maxFps')) {
            $lines = @($lines | Where-Object { !($_.StartsWith($name + ':')) })
            $lines += $name + ':' + [int]$session.$name
        }
        [IO.File]::WriteAllLines($optionsPath, [string[]]$lines)
    } elseif (Test-Path -LiteralPath $backupPath) {
        # Restore only the settings managed by construction mode, retaining unrelated preferences.
        $before = @(Get-Content -LiteralPath $backupPath)
        $lines = if (Test-Path -LiteralPath $optionsPath) { @(Get-Content -LiteralPath $optionsPath) } else { @() }
        foreach ($name in @('renderDistance', 'simulationDistance', 'maxFps')) {
            $lines = @($lines | Where-Object { !($_.StartsWith($name + ':')) })
            $lines += @($before | Where-Object { $_.StartsWith($name + ':') })
        }
        [IO.File]::WriteAllLines($optionsPath, [string[]]$lines)
        Remove-Item -LiteralPath $backupPath
    }
}
$taskName = switch ($Action) {
    'prepare' { 'createMinecraftArtifacts' }
    'build' { 'build' }
    'run' { 'runClient' }
    'debug' { 'runClient' }
}
Push-Location (Join-Path $projectRoot 'mods/dbv-world')
try {
    $gradleArgs = @($taskName, '--console=plain')
    # A daemon started by automation can belong to a non-interactive desktop.
    # Launch the game from this invocation instead of reusing that daemon.
    if ($Action -in @('run', 'debug')) {
        $gradleArgs += '--no-daemon'
        Write-Host 'Starting Minecraft in a separate window. Keep this terminal open.'
        if ($Action -eq 'debug') {
            Write-Host 'Debug mode waits on port 5005. Attach the VS Code debugger to continue.'
        }
    }
    $gradleArgs += '-PdbvConstruction=' + $construction.ToString().ToLowerInvariant()
    if ($Action -eq 'debug') { $gradleArgs += '--debug-jvm' }
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
} finally { Pop-Location }
