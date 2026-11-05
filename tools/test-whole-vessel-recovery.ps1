param(
    [ValidateSet('prepare','run')][string]$Action = 'prepare',
    [string]$RunDirectory = '',
    [string]$JavaHome = $env:JAVA_HOME,
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$repo = Split-Path $PSScriptRoot -Parent
$acceptanceRoot = [IO.Path]::GetFullPath((Join-Path $repo '.local/acceptance/whole-vessel-recovery'))
$utf8 = New-Object System.Text.UTF8Encoding($false)
function Write-Text([string]$Path,[string]$Content) { [IO.File]::WriteAllText($Path,$Content,$utf8) }
function Assert-TestPath([string]$Path) {
    $full = [IO.Path]::GetFullPath($Path)
    if (!$full.StartsWith($acceptanceRoot.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase)) { throw "验收路径必须位于 $acceptanceRoot 内：$full" }
    return $full
}
if (!$JavaHome) {
    $settingsFile = Join-Path $repo '.vscode/settings.json'
    if (Test-Path -LiteralPath $settingsFile) { $JavaHome = (Get-Content -Raw -Encoding UTF8 -LiteralPath $settingsFile | ConvertFrom-Json).'java.jdt.ls.java.home' }
}
if (!$JavaHome -or !(Test-Path -LiteralPath (Join-Path $JavaHome 'bin/javac.exe'))) { throw '请通过 JAVA_HOME 或 -JavaHome 指定 Zulu JDK 21。' }
$java = Join-Path $JavaHome 'bin/java.exe'
$env:JAVA_HOME = $JavaHome
$env:GRADLE_USER_HOME = Join-Path $repo '.local/gradle-home'
$neo = [regex]::Match((Get-Content -Raw -LiteralPath (Join-Path $repo 'mods/dbv-world/gradle.properties')),'(?m)^neo_version=(\S+)').Groups[1].Value
if (!$neo) { throw '无法确定 NeoForge 版本。' }
New-Item -ItemType Directory -Force -Path $acceptanceRoot | Out-Null
if ($Action -eq 'prepare') {
    if (!$RunDirectory) { $RunDirectory = Join-Path $acceptanceRoot ((Get-Date -Format 'yyyyMMdd-HHmmss')+'-'+[guid]::NewGuid().ToString('N').Substring(0,8)) }
    $run = Assert-TestPath $RunDirectory
    if (Test-Path -LiteralPath $run) { throw 'prepare 只创建新的验收目录，不覆盖已有验收世界。' }
    New-Item -ItemType Directory -Path $run | Out-Null
    Write-Text (Join-Path $run '.dbv-recovery-probe') 'This directory contains a destructive, isolated whole-vessel recovery test.'
    $stage = Join-Path $run 'pack'
    $probeOutput = Join-Path $run 'probe'
    $gradle = @('-p',(Join-Path $repo 'mods/dbv-world'),'-I',(Join-Path $repo 'server/distribution.gradle'),'-I',(Join-Path $PSScriptRoot 'whole-vessel-recovery.gradle'),"-PcoopOutput=$stage","-PdbvRecoveryOutput=$probeOutput",'exportCoopPack','recoveryProbeJar','--console=plain')
    if ($Offline) { $gradle += '--offline' }
    & (Join-Path $repo 'mods/dbv-world/gradlew.bat') @gradle
    if ($LASTEXITCODE -ne 0) { throw '构建或探针编译失败，未启动任何游戏。' }
    $instance = Join-Path $run 'server'
    New-Item -ItemType Directory -Path $instance | Out-Null
    Copy-Item -LiteralPath (Join-Path $stage 'server/mods') -Destination (Join-Path $instance 'mods') -Recurse
    Copy-Item -LiteralPath (Join-Path $probeOutput 'dbv-recovery-probe.jar') -Destination (Join-Path $instance 'mods')
    foreach ($folder in @('config','defaultconfigs','kubejs')) {
        $source = Join-Path $repo "pack/overrides/$folder"
        if (Test-Path -LiteralPath $source) { Copy-Item -LiteralPath $source -Destination (Join-Path $instance $folder) -Recurse }
    }
    $installedLibraries = Join-Path $repo 'runtime/server/libraries'
    if (Test-Path -LiteralPath (Join-Path $installedLibraries "net/neoforged/neoforge/$neo/win_args.txt")) {
        # Read only the installed loader libraries. Never copy or modify a player world or server configuration.
        Copy-Item -LiteralPath $installedLibraries -Destination (Join-Path $instance 'libraries') -Recurse
    } else {
        $installer = Join-Path $repo ".local/server-installers/neoforge-$neo-installer.jar"
        if (!(Test-Path -LiteralPath $installer)) { throw '缺少 NeoForge 安装器。请先按 server/README.md 准备服务端环境，再运行验收准备。' }
        $hashFile = "$installer.sha256"
        if (!(Test-Path -LiteralPath $hashFile)) { throw '缺少安装器 SHA-256 校验文件。' }
        $expected = ((Get-Content -Raw -LiteralPath $hashFile).Trim() -split '\s+')[0]
        if ((Get-FileHash -LiteralPath $installer -Algorithm SHA256).Hash -ne $expected) { throw 'NeoForge 安装器校验失败。' }
        Push-Location $instance
        try {
            & $java -jar $installer --installServer
            if ($LASTEXITCODE -ne 0) { throw '独立验收服务端安装失败。' }
        } finally { Pop-Location }
    }
    $acceptedEula = Join-Path $repo 'runtime/server/eula.txt'
    if ((Test-Path -LiteralPath $acceptedEula) -and (Get-Content -Raw -LiteralPath $acceptedEula) -match '(?m)^\s*eula=true\s*$') {
        Write-Text (Join-Path $instance 'eula.txt') "eula=true`n"
    } else { Write-Text (Join-Path $instance 'eula.txt') "eula=false`n" }
    Write-Text (Join-Path $instance 'server.properties') @'
level-name=world
level-type=minecraft:flat
generator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:the_void","structures":{}}
level-seed=20261105
gamemode=creative
difficulty=peaceful
generate-structures=false
spawn-protection=0
spawn-monsters=false
spawn-animals=false
spawn-npcs=false
online-mode=false
enforce-secure-profile=false
server-ip=127.0.0.1
server-port=25667
enable-query=false
enable-rcon=false
view-distance=3
simulation-distance=3
max-players=1
max-tick-time=120000
pause-when-empty-seconds=-1
'@
    Write-Text (Join-Path $instance 'user_jvm_args.txt') "-Xms1G`n-Xmx4G`n-XX:ActiveProcessorCount=4`n-Dfile.encoding=UTF-8`n-Ddbv.construction=true`n-Ddbv.recoveryProbe=true`n"
    $manifest = @{ minecraft='1.21.1'; neoforge=$neo; prepared=(Get-Date).ToString('o'); phases=@('recover','restart','resume','splitrollback'); mods=@(Get-ChildItem -LiteralPath (Join-Path $instance 'mods') -Filter '*.jar' | Sort-Object Name | ForEach-Object { @{ file=$_.Name; sha256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant() } }) }
    Write-Text (Join-Path $run 'manifest.json') ($manifest | ConvertTo-Json -Depth 5)
    Write-Text (Join-Path $acceptanceRoot 'latest-run.txt') $run
    Write-Host "已准备独立验收目录，尚未启动：$run"
    Write-Host "运行命令：powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\test-whole-vessel-recovery.ps1 run -RunDirectory `"$run`""
    return
}
if (!$RunDirectory) {
    $latest = Join-Path $acceptanceRoot 'latest-run.txt'
    if (!(Test-Path -LiteralPath $latest)) { throw '请先执行 prepare。' }
    $RunDirectory = (Get-Content -Raw -LiteralPath $latest).Trim()
}
$run = Assert-TestPath $RunDirectory
if (!(Test-Path -LiteralPath (Join-Path $run '.dbv-recovery-probe'))) { throw '验收标记缺失，拒绝运行。' }
$instance = Join-Path $run 'server'
$manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath (Join-Path $run 'manifest.json') | ConvertFrom-Json
foreach ($mod in $manifest.mods) {
    $file = Join-Path $instance "mods/$($mod.file)"
    if (!(Test-Path -LiteralPath $file) -or (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash -ne $mod.sha256) { throw "验收 Mod 被修改：$file。请建立新的验收目录。" }
}
if ((Get-Content -Raw -LiteralPath (Join-Path $instance 'eula.txt')) -notmatch '(?m)^\s*eula=true\s*$') { throw '请先阅读 Minecraft EULA，并在此验收目录的 server/eula.txt 中确认。' }
$lock = [IO.File]::Open((Join-Path $run 'operation.lock'),'OpenOrCreate','ReadWrite','None')
try {
    foreach ($phase in @('recover','restart','resume','splitrollback')) {
        $report = Join-Path $run "reports/$phase.json"
        if (Test-Path -LiteralPath $report) {
            $previous = Get-Content -Raw -Encoding UTF8 -LiteralPath $report | ConvertFrom-Json
            if ($previous.passed) { continue }
            throw "上一轮 $phase 失败；保留证据并创建新的验收目录。"
        }
        Push-Location $instance
        try {
            Write-Host "开始无界面验收阶段：$phase；仅使用 $instance/world"
            $savedErrorPreference = $ErrorActionPreference
            $savedConsoleEncoding = [Console]::OutputEncoding
            try {
                # Windows PowerShell 5.1 wraps native stderr as NativeCommandError records.
                # Java warnings must reach the UTF-8 log without skipping the exit/report checks below.
                $ErrorActionPreference = 'Continue'
                [Console]::OutputEncoding = $utf8
                & $java '@user_jvm_args.txt' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' "-Ddbv.recoveryProbe.directory=$run" "-Ddbv.recoveryProbe.phase=$phase" "@libraries/net/neoforged/neoforge/$($manifest.neoforge)/win_args.txt" nogui 2>&1 |
                    ForEach-Object { $_.ToString() } | Out-File -LiteralPath (Join-Path $run "$phase-console.log") -Encoding utf8
                $exit = $LASTEXITCODE
            } finally {
                $ErrorActionPreference = $savedErrorPreference
                [Console]::OutputEncoding = $savedConsoleEncoding
            }
        } finally { Pop-Location }
        if ($exit -ne 0 -or !(Test-Path -LiteralPath $report)) { throw "服务端退出代码 $exit，或未产生 $phase 报告。查看 $run/$phase-console.log。" }
        $result = Get-Content -Raw -Encoding UTF8 -LiteralPath $report | ConvertFrom-Json
        if (!$result.passed) { throw "验收阶段 $phase 未通过：$($result.failure)" }
        Write-Host "通过：$phase"
    }
    Write-Host "四阶段验收通过。结果：$run/reports；正式客户端、服务器和玩家存档均未修改。"
} finally { $lock.Dispose() }
