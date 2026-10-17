$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jdk = $env:JAVA_HOME
$settingsPath = Join-Path $projectRoot '.vscode/settings.json'
if (Test-Path -LiteralPath $settingsPath) {
    $settings = Get-Content -LiteralPath $settingsPath -Raw | ConvertFrom-Json
    if ($settings.'java.jdt.ls.java.home') { $jdk = $settings.'java.jdt.ls.java.home' }
}
if (!$jdk) { throw '请将 JAVA_HOME 指向 Java 21 开发工具包的安装目录。' }
foreach ($executable in @('java.exe', 'javac.exe', 'jar.exe')) {
    if (!(Test-Path -LiteralPath (Join-Path $jdk "bin/$executable"))) {
        throw "开发工具包缺少可执行文件：$executable，所在目录：$jdk"
    }
}
$release = Get-Content -LiteralPath (Join-Path $jdk 'release') -Raw
if ($release -notmatch '(?m)^JAVA_VERSION="21(?:\.|"|\+)') {
    throw '此工作区需要 Java 21 开发工具包。'
}
& (Join-Path $jdk 'bin/java.exe') -version
if ($LASTEXITCODE -ne 0) { throw 'Java 运行环境检查失败。' }
& (Join-Path $jdk 'bin/javac.exe') -version
if ($LASTEXITCODE -ne 0) { throw 'Java 编译器检查失败。' }
git --version
if ($LASTEXITCODE -ne 0) { throw '版本管理工具检查失败。' }
Write-Output '环境检查通过：Java 21 和版本管理工具均可用。'
