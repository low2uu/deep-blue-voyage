$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jdk = $env:JAVA_HOME
$settingsPath = Join-Path $projectRoot '.vscode/settings.json'
if (Test-Path -LiteralPath $settingsPath) {
    $settings = Get-Content -LiteralPath $settingsPath -Raw | ConvertFrom-Json
    if ($settings.'java.jdt.ls.java.home') { $jdk = $settings.'java.jdt.ls.java.home' }
}
if (!$jdk) { throw 'Set JAVA_HOME to your JDK 21 installation.' }
foreach ($executable in @('java.exe', 'javac.exe', 'jar.exe')) {
    if (!(Test-Path -LiteralPath (Join-Path $jdk "bin/$executable"))) {
        throw "Missing JDK executable: $executable in $jdk"
    }
}
$release = Get-Content -LiteralPath (Join-Path $jdk 'release') -Raw
if ($release -notmatch '(?m)^JAVA_VERSION="21(?:\.|"|\+)') {
    throw 'This workspace requires JDK 21.'
}
& (Join-Path $jdk 'bin/java.exe') -version
if ($LASTEXITCODE -ne 0) { throw 'Java check failed.' }
& (Join-Path $jdk 'bin/javac.exe') -version
if ($LASTEXITCODE -ne 0) { throw 'Java compiler check failed.' }
git --version
if ($LASTEXITCODE -ne 0) { throw 'Git check failed.' }
Write-Output 'Environment OK: Java 21 and Git are available.'
