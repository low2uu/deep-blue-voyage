param([switch]$PrintPath)
$ErrorActionPreference = 'Stop'
$page = Join-Path $PSScriptRoot 'content-designer/index.html'
if (!(Test-Path -LiteralPath $page)) { throw '找不到内容设计器，请检查 tools/content-designer 目录是否完整。' }
if ($PrintPath) { Write-Output $page; exit 0 }
Start-Process -FilePath $page
