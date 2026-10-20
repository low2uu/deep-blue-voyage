param(
    [Parameter(Position=0)]
    [ValidateSet('prepare','start','update','backup','client-pack','status','offline-ipv6')]
    [string]$Action = 'status',
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Players = ''
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$repo = Split-Path $PSScriptRoot -Parent
$homeDir = Join-Path $repo 'runtime/server'
$backupDir = Join-Path $repo 'runtime/server-backups'
$utf8 = New-Object System.Text.UTF8Encoding($false)
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Write-Text($Path, $Text) {
    [IO.File]::WriteAllText($Path, $Text, $utf8)
}
function Move-Safely($Source, $Destination) {
    $rootPrefix = [IO.Path]::GetFullPath($repo).TrimEnd('\') + '\'
    foreach ($candidate in @($Source, $Destination)) {
        $full = [IO.Path]::GetFullPath($candidate)
        if (!$full.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
            throw "移动路径超出项目目录：$full"
        }
    }
    Move-Item -LiteralPath $Source -Destination $Destination
}
function Git-Text([string[]]$Arguments) {
    $result = & git -C $repo @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Git 命令失败：$Arguments" }
    return ($result -join "`n")
}
function Backup-Server {
    if (!(Test-Path $homeDir)) { throw '尚未部署服务器。' }
    New-Item -ItemType Directory -Force $backupDir | Out-Null
    $path = Join-Path $backupDir ("server-{0}-{1}.zip" -f (Get-Date -Format 'yyyyMMdd-HHmmss'), [guid]::NewGuid().ToString('N').Substring(0,8))
    [IO.Compression.ZipFile]::CreateFromDirectory($homeDir, "$path.partial", [IO.Compression.CompressionLevel]::Fastest, $false)
    Move-Safely "$path.partial" $path
    Write-Text "$path.sha256" ((Get-FileHash $path -Algorithm SHA256).Hash.ToLowerInvariant())
    Write-Host "完整备份：$path"
}
function Set-OfflineIPv6 {
    $properties = Join-Path $homeDir 'server.properties'
    if (!(Test-Path $properties)) { throw '请先执行 prepare。' }
    $names = @()
    $whitePath = Join-Path $homeDir 'whitelist.json'
    if (Test-Path $whitePath) {
        $existing = Get-Content $whitePath -Raw -Encoding UTF8 | ConvertFrom-Json
        foreach ($entry in $existing) { $names += $entry.name }
    }
    $names += @($Players -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    foreach ($name in $names) {
        if ($name -cnotmatch '^[A-Za-z0-9_]{3,16}$') { throw "昵称应为 3 至 16 位英文字母、数字或下划线：$name" }
    }
    Backup-Server
    $text = [IO.File]::ReadAllText($properties)
    foreach ($entry in @{'online-mode'='false';'enforce-secure-profile'='false';'server-ip'='::';'white-list'='true';'enforce-whitelist'='true'}.GetEnumerator()) {
        $pattern = '(?m)^' + [regex]::Escape($entry.Key) + '\s*=.*$'
        $line = $entry.Key + '=' + $entry.Value
        if ([regex]::IsMatch($text, $pattern)) { $text = [regex]::Replace($text, $pattern, $line) }
        else { $text += "`n$line`n" }
    }
    $entries = @($names | Select-Object -Unique | ForEach-Object {
        $md5 = [Security.Cryptography.MD5]::Create()
        try { $bytes = $md5.ComputeHash([Text.Encoding]::UTF8.GetBytes('OfflinePlayer:' + $_)) }
        finally { $md5.Dispose() }
        $bytes[6] = ($bytes[6] -band 15) -bor 48
        $bytes[8] = ($bytes[8] -band 63) -bor 128
        $hex = [BitConverter]::ToString($bytes).Replace('-','').ToLowerInvariant()
        $uuid = $hex.Substring(0,8)+'-'+$hex.Substring(8,4)+'-'+$hex.Substring(12,4)+'-'+$hex.Substring(16,4)+'-'+$hex.Substring(20,12)
        @{name=$_;uuid=$uuid}
    })
    Write-Text $whitePath (ConvertTo-Json -InputObject $entries -Depth 4)
    Write-Text $properties $text
    Write-Host '已启用离线登录与 IPv6 监听，白名单已按离线 UUID 写入。昵称和大小写必须固定。'
    Write-Host '旧正版玩家数据与 OP 身份不会自动迁移。玩家加入后，在控制台重新 op 对应昵称。'
}

function Export-Pack {
    $stage = Join-Path $repo ("dist/coop/{0}-{1}" -f (Get-Date -Format 'yyyyMMdd-HHmmss'), [guid]::NewGuid().ToString('N').Substring(0,8))
    & (Join-Path $repo 'mods/dbv-world/gradlew.bat') -p (Join-Path $repo 'mods/dbv-world') -I (Join-Path $PSScriptRoot 'distribution.gradle') "-PcoopOutput=$stage" exportCoopPack --no-daemon --console=plain | Out-Host
    if ($LASTEXITCODE -ne 0) { throw '构建失败，未替换服务端 Mod。' }
    foreach ($side in @('server','client')) {
        $target = Join-Path $stage $side
        foreach ($folder in @('config','defaultconfigs','kubejs')) {
            $source = Join-Path $repo "pack/overrides/$folder"
            if (Test-Path $source) { Copy-Item -LiteralPath $source -Destination $target -Recurse }
        }
        if ($side -eq 'client') {
            Copy-Item -LiteralPath (Join-Path $repo 'pack/overrides/resourcepacks') -Destination $target -Recurse
        }
        $files = @(Get-ChildItem (Join-Path $target 'mods') -Filter '*.jar' | Sort-Object Name | ForEach-Object {
            @{file=$_.Name; sha256=(Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()}
        })
        $record = @{
            commit=(Git-Text @('rev-parse','HEAD')); dirty=([bool](Git-Text @('status','--porcelain','--untracked-files=no')))
            minecraft='1.21.1'; neoforge=$neo; side=$side; created=(Get-Date).ToString('o'); mods=$files
        }
        Write-Text (Join-Path $target 'pack-version.json') ($record | ConvertTo-Json -Depth 6)
    }
    $zip = Join-Path $stage 'client-pack.zip'
    [IO.Compression.ZipFile]::CreateFromDirectory((Join-Path $stage 'client'), $zip)
    Write-Host "客户端更新包：$zip"
    return $stage
}
function Prepare-Server {
    $stage = Export-Pack
    New-Item -ItemType Directory -Force $homeDir | Out-Null
    $argsFile = Join-Path $homeDir "libraries/net/neoforged/neoforge/$neo/win_args.txt"
    $installMarker = Join-Path $homeDir "neoforge-$neo.installed"
    if (!(Test-Path $argsFile) -or !(Test-Path $installMarker)) {
        $cache = Join-Path $repo '.local/server-installers'
        New-Item -ItemType Directory -Force $cache | Out-Null
        $installer = Join-Path $cache "neoforge-$neo-installer.jar"
        $url = "https://maven.neoforged.net/releases/net/neoforged/neoforge/$neo/neoforge-$neo-installer.jar"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -UseBasicParsing $url -OutFile $installer
        Invoke-WebRequest -UseBasicParsing "$url.sha256" -OutFile "$installer.sha256"
        $expected = ((Get-Content "$installer.sha256" -Raw).Trim() -split '\s+')[0]
        if ((Get-FileHash $installer -Algorithm SHA256).Hash -ne $expected) { throw 'NeoForge 安装器校验失败。' }
        Push-Location $homeDir
        try {
            Write-Host "正在安装 NeoForge，详细日志：$homeDir/installer-console.log"
            & $script:java -jar $installer --installServer | Out-File (Join-Path $homeDir 'installer-console.log') -Encoding UTF8
            if ($LASTEXITCODE -ne 0) { throw 'NeoForge 安装失败。' }
            Write-Text $installMarker $neo
        } finally { Pop-Location }
    }
    if (!(Test-Path (Join-Path $homeDir 'world'))) {
        & (Join-Path $repo 'tools/restore-world.ps1') -Destination $homeDir
        $manifest = Get-Content (Join-Path $repo 'pack/worlds/world-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
        Move-Safely (Join-Path $homeDir $manifest.folder) (Join-Path $homeDir 'world')
    }
    # Replace the managed mods folder, never merge old versions into it.
    $modsDir = Join-Path $homeDir 'mods'
    $oldMods = Join-Path $repo (".local/server-mods-previous-" + [guid]::NewGuid().ToString('N'))
    if (Test-Path $modsDir) { Move-Safely $modsDir $oldMods }
    try { Copy-Item -LiteralPath (Join-Path $stage 'server/mods') -Destination $modsDir -Recurse }
    catch {
        if (Test-Path $oldMods) {
            if (Test-Path $modsDir) { Move-Safely $modsDir "$modsDir.failed-$([guid]::NewGuid().ToString('N'))" }
            Move-Safely $oldMods $modsDir
        }
        throw
    }
    foreach ($folder in @('config','defaultconfigs','kubejs')) {
        $source = Join-Path $stage "server/$folder"
        if ((Test-Path $source) -and !(Test-Path (Join-Path $homeDir $folder))) {
            Copy-Item -LiteralPath $source -Destination $homeDir -Recurse
        }
    }
    if (!(Test-Path (Join-Path $homeDir 'server.properties'))) {
        Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'server.properties.example') -Destination (Join-Path $homeDir 'server.properties')
    }
    if (!(Test-Path (Join-Path $homeDir 'user_jvm_args.txt'))) {
        Write-Text (Join-Path $homeDir 'user_jvm_args.txt') "-Xms2G`n-Xmx6G`n-Dfile.encoding=UTF-8`n-Ddbv.construction=true`n"
    } elseif (!(Test-Path (Join-Path $homeDir 'pack-version.json'))) {
        # Installer supplies a comments-only default on the first deployment.
        Add-Content -LiteralPath (Join-Path $homeDir 'user_jvm_args.txt') -Value "`n-Xms2G`n-Xmx6G`n-Dfile.encoding=UTF-8`n-Ddbv.construction=true" -Encoding UTF8
    }
    if (!(Test-Path (Join-Path $homeDir 'eula.txt'))) { Write-Text (Join-Path $homeDir 'eula.txt') "eula=false`n" }
    Copy-Item -LiteralPath (Join-Path $stage 'server/pack-version.json') -Destination $homeDir -Force
    Write-Host "部署完成：$homeDir。首次开服请阅读 README，手动确认 EULA 并配置白名单。"
}

Push-Location $repo
$lock = $null
try {
    if ($Action -eq 'status') {
        if (Test-Path (Join-Path $homeDir 'pack-version.json')) { Get-Content (Join-Path $homeDir 'pack-version.json') -Encoding UTF8 }
        else { Write-Host '尚未部署；先执行 prepare。' }
        return
    }
    New-Item -ItemType Directory -Force (Join-Path $repo 'runtime') | Out-Null
    try { $lock = [IO.File]::Open((Join-Path $repo 'runtime/server.operation.lock'), 'OpenOrCreate', 'ReadWrite', 'None') }
    catch { throw '服务端或维护操作仍在运行。请在服务器窗口输入 stop，等待退出后重试。' }
    if ($Action -eq 'backup') { Backup-Server; return }
    if ($Action -eq 'offline-ipv6') { Set-OfflineIPv6; return }
    if (!$JavaHome -or !(Test-Path (Join-Path $JavaHome 'bin/java.exe'))) { throw '请设置 JAVA_HOME 为 Zulu JDK 21 安装目录，或传入 -JavaHome。' }
    $script:java = Join-Path $JavaHome 'bin/java.exe'
    $info = (& $script:java --version | Out-String)
    if ($info -notmatch '(?m)^(openjdk|java) 21[\.+ ]') { throw "需要 JDK 21。当前：$info" }
    if (!(Test-Path (Join-Path $JavaHome 'bin/javac.exe'))) { throw '需要完整 JDK，而不是 JRE。' }
    $env:JAVA_HOME = $JavaHome
    $env:GRADLE_USER_HOME = Join-Path $repo '.local/gradle-home'
    if ($Action -eq 'update') {
        if ((Git-Text @('branch','--show-current')) -ne 'develop') { throw '自动更新只支持 develop 分支。' }
        if (Git-Text @('status','--porcelain')) { throw '工作区有未提交文件，请先自行提交或移走；脚本不会清理它们。' }
        Backup-Server
        & git pull --ff-only origin develop
        if ($LASTEXITCODE -ne 0) { throw 'Git 拉取失败，停止更新。' }
    }
    $neoMatch = [regex]::Match((Get-Content (Join-Path $repo 'mods/dbv-world/gradle.properties') -Raw), '(?m)^neo_version=(\S+)')
    if (!$neoMatch.Success) { throw '没有找到 NeoForge 版本。' }
    $script:neo = $neoMatch.Groups[1].Value
    switch ($Action) {
        'client-pack' { $null = Export-Pack }
        'prepare' {
            if (Test-Path (Join-Path $homeDir 'pack-version.json')) { Backup-Server }
            Prepare-Server
        }
        'update' { Prepare-Server }
        'start' {
            $versionFile = Join-Path $homeDir 'pack-version.json'
            if (!(Test-Path $versionFile)) { throw '请先执行 prepare。' }
            $installed = Get-Content $versionFile -Raw -Encoding UTF8 | ConvertFrom-Json
            if (@(Get-ChildItem (Join-Path $homeDir 'mods') -Filter '*.jar').Count -ne @($installed.mods).Count) { throw 'Mod 数量与版本清单不一致，请重新 prepare，不要混装额外 Mod。' }
            foreach ($mod in $installed.mods) {
                $path = Join-Path $homeDir "mods/$($mod.file)"
                if (!(Test-Path $path) -or (Get-FileHash $path -Algorithm SHA256).Hash -ne $mod.sha256) { throw "Mod 缺失或被修改：$($mod.file)，请重新 prepare。" }
            }
            $argsFile = "libraries/net/neoforged/neoforge/$($installed.neoforge)/win_args.txt"
            if ((Get-Content (Join-Path $homeDir 'eula.txt') -Raw) -notmatch '(?m)^\s*eula=true\s*$') { throw '请先阅读 Minecraft EULA，自行在 runtime/server/eula.txt 中确认。' }
            Push-Location $homeDir
            $oldOut = [Console]::OutputEncoding
            $oldIn = [Console]::InputEncoding
            try {
                [Console]::OutputEncoding = $utf8
                [Console]::InputEncoding = $utf8
                & $script:java '@user_jvm_args.txt' '-Djava.net.preferIPv4Stack=false' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' "@$argsFile" nogui
                if ($LASTEXITCODE -ne 0) { throw '服务器异常退出，请查看 runtime/server/logs/latest.log。' }
            } finally {
                [Console]::OutputEncoding = $oldOut
                [Console]::InputEncoding = $oldIn
                Pop-Location
            }
        }
    }
} finally {
    if ($null -ne $lock) { $lock.Dispose() }
    Pop-Location
}
