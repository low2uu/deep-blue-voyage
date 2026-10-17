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
    throw '请通过 JAVA_HOME 或 .vscode/settings.json 配置 Java 21 开发工具包。'
}
$env:JAVA_HOME = $jdkPath
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.local/gradle-home'
$session = Get-Content (Join-Path $projectRoot 'config/development-session.json') -Raw | ConvertFrom-Json
if ($session.constructionMode -isnot [bool]) { throw '建设模式配置 constructionMode 必须为布尔值（true 或 false）。' }
foreach ($name in @('renderDistance', 'simulationDistance', 'maxFps')) {
    $value = $session.$name
    $maximum = if ($name -eq 'maxFps') { 260 } else { 32 }
    $minimum = if ($name -eq 'maxFps') { 10 } elseif ($name -eq 'simulationDistance') { 5 } else { 2 }
    if ($null -eq $value -or $value -is [string] -or [double]$value -ne [int]$value -or $value -lt $minimum -or $value -gt $maximum) {
        throw "配置项 $name 必须是 $minimum 到 $maximum 之间的整数。"
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
        Write-Host '正在独立窗口中启动游戏，请保持此终端开启。'
        if ($Action -eq 'debug') {
            Write-Host '调试模式正在等待连接端口 5005，请连接编辑器调试器以继续启动。'
        }
    }
    $gradleArgs += '-PdbvConstruction=' + $construction.ToString().ToLowerInvariant()
    if ($Action -eq 'debug') { $gradleArgs += '--debug-jvm' }
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) { throw "构建工具执行失败，退出代码：$LASTEXITCODE" }
} finally { Pop-Location }
