param([string]$Version = '')
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
if (!$Version) {
    $gradle = [IO.File]::ReadAllText((Join-Path $project 'mods/dbv-world/build.gradle'))
    $Version = [regex]::Match($gradle, "(?m)^version = '([^']+)'").Groups[1].Value
}
if ($Version -notmatch '^\d+\.\d+\.\d+$') { throw 'Expected a numeric major.minor.patch version.' }
$source = Join-Path $PSScriptRoot 'content-designer'
$stageRoot = Join-Path $project ('.local/content-designer-package-' + [guid]::NewGuid().ToString('N'))
$stage = Join-Path $stageRoot 'content-designer'
$destination = Join-Path $project 'tool/content-designer'
$archive = Join-Path $project "tool/content-designer-v$Version.zip"
if (Test-Path -LiteralPath $archive) { throw "Archive already exists; do not replace a released package: $archive" }
New-Item -ItemType Directory -Path $stage -Force | Out-Null
$files = @('index.html','style.css','app.js','catalog.js','core.js','content-spec.schema.json','iron-submarine-hull.example.json','validate.cjs','test.cjs')
foreach ($name in $files) { Copy-Item -LiteralPath (Join-Path $source $name) -Destination (Join-Path $stage $name) }
Copy-Item -LiteralPath (Join-Path $PSScriptRoot '设计器便携说明-content-designer-portable.md') -Destination (Join-Path $stage 'README.md')
[IO.File]::WriteAllText((Join-Path $stage 'start.cmd'), "@echo off`r`nstart `"`" `"%~dp0index.html`"`r`n", [Text.Encoding]::ASCII)
New-Item -ItemType Directory -Path (Join-Path $stage 'docs') -Force | Out-Null
foreach ($name in @('内容设计器设计与使用-content-designer.md','内容设计器字段目录-content-designer-fields.md','方块属性规范与对照表-block-property-specification.md')) {
    Copy-Item -LiteralPath (Join-Path $project "docs/development/$name") -Destination (Join-Path $stage "docs/$name")
}
$manifest = [ordered]@{ package = 'dbv-content-designer'; version = $Version; schemaVersion = 1; files = @() }
foreach ($file in Get-ChildItem -LiteralPath $stage -File -Recurse | Sort-Object FullName) {
    $manifest.files += [ordered]@{ path = $file.FullName.Substring($stage.Length + 1).Replace('\','/'); sha256 = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant() }
}
[IO.File]::WriteAllText((Join-Path $stage 'manifest.json'), ($manifest | ConvertTo-Json -Depth 5), [Text.UTF8Encoding]::new($false))
New-Item -ItemType Directory -Path $destination -Force | Out-Null
foreach ($entry in Get-ChildItem -LiteralPath $stage) { Copy-Item -LiteralPath $entry.FullName -Destination $destination -Recurse -Force }
Compress-Archive -LiteralPath $stage -DestinationPath $archive -CompressionLevel Optimal
$hash = (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant()
[IO.File]::WriteAllText("$archive.sha256", "$hash  $([IO.Path]::GetFileName($archive))`n", [Text.Encoding]::ASCII)
Write-Output "Portable directory: $destination"
Write-Output "ZIP: $archive"
Write-Output "SHA256: $hash"
