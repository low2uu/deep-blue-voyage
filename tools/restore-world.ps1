param(
    [switch]$VerifyOnly,
    [string]$Destination = ''
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$packRoot = Join-Path $projectRoot 'pack/worlds'
$manifest = Get-Content -LiteralPath (Join-Path $packRoot 'world-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($name in @($manifest.folder, $manifest.archive)) {
    if (!$name -or [IO.Path]::GetFileName($name) -ne $name -or $name -in @('.', '..')) { throw '存档清单中的路径无效。' }
}
if (!$Destination) { $Destination = Join-Path $projectRoot 'runtime/client/saves' }
$Destination = [IO.Path]::GetFullPath($Destination)
$target = Join-Path $Destination $manifest.folder
if (!$VerifyOnly -and (Test-Path -LiteralPath $target)) { throw "目标存档已存在，拒绝覆盖：$target" }
$tempParent = [IO.Path]::GetFullPath((Join-Path $projectRoot 'runtime'))
$tempRoot = Join-Path $tempParent ('world-import-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null
try {
    $archive = Join-Path $tempRoot $manifest.archive
    $parts = @($manifest.parts)
    if ($parts.Count -eq 0) { $parts = @([pscustomobject]@{file=$manifest.archive;size=$manifest.size;sha256=$manifest.sha256}) }
    $output = [IO.File]::Open($archive, 'CreateNew', 'Write', 'None')
    try {
        foreach ($part in $parts) {
            if ([IO.Path]::GetFileName($part.file) -ne $part.file) { throw '分卷文件名无效。' }
            $path = Join-Path $packRoot $part.file
            if ((Get-Item -LiteralPath $path).Length -ne $part.size -or (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $part.sha256) { throw "分卷校验失败：$($part.file)" }
            $inputStream = [IO.File]::OpenRead($path)
            try { $inputStream.CopyTo($output) } finally { $inputStream.Dispose() }
        }
    } finally { $output.Dispose() }
    if ((Get-Item -LiteralPath $archive).Length -ne $manifest.size -or (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash -ne $manifest.sha256) { throw '完整存档校验失败。' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead($archive)
    try {
        if (!$zip.GetEntry($manifest.folder + '/level.dat')) { throw '压缩包缺少世界信息。' }
        foreach ($entry in $zip.Entries) {
            $name = $entry.FullName.Replace('\', '/')
            if (!$name.StartsWith($manifest.folder + '/', [StringComparison]::Ordinal) -or ($name.Split('/') -contains '..')) { throw '压缩包含越界路径。' }
        }
    } finally { $zip.Dispose() }
    if ($VerifyOnly) { Write-Output "存档 v$($manifest.version) 的全部分卷与完整 SHA-256 校验通过。" }
    else {
        $unpacked = Join-Path $tempRoot 'unpacked'
        [IO.Compression.ZipFile]::ExtractToDirectory($archive, $unpacked)
        New-Item -ItemType Directory -Path $Destination -Force | Out-Null
        if (Test-Path -LiteralPath $target) { throw '目标存档在导入期间出现，拒绝覆盖。' }
        $source = [IO.Path]::GetFullPath((Join-Path $unpacked $manifest.folder))
        if (!$source.StartsWith($tempRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw '导入路径越界。' }
        Move-Item -LiteralPath $source -Destination $target
        Write-Output "已导入 v$($manifest.version)：$target"
    }
} finally {
    $resolved = [IO.Path]::GetFullPath($tempRoot)
    if ($resolved.StartsWith($tempParent + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase) -and [IO.Path]::GetFileName($resolved).StartsWith('world-import-')) {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    }
}
