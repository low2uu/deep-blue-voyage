param([string]$RuntimeDirectory)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (!$RuntimeDirectory) { $RuntimeDirectory = Join-Path $projectRoot 'runtime/client' }
$balance = Get-Content (Join-Path $projectRoot 'config/submarine-balance.json') -Raw | ConvertFrom-Json
$lift = [double]$balance.ballastLiftPerTank
if ([double]::IsNaN($lift) -or $lift -lt 1 -or $lift -gt 100000) { throw 'Invalid ballastLiftPerTank.' }
$directory = Join-Path $RuntimeDirectory 'config'
New-Item -ItemType Directory -Force -Path $directory | Out-Null
$file = Join-Path $directory 'create_submarine-server.toml'
$value = $lift.ToString('0.0', [Globalization.CultureInfo]::InvariantCulture)
if (Test-Path -LiteralPath $file) {
    $text = [IO.File]::ReadAllText($file)
    if ($text -notmatch '(?m)^\s*ballastLiftPerTank\s*=') { throw 'Existing Deep Seas config has no ballastLiftPerTank; preserve it and inspect the version.' }
    $updated = $text -replace '(?m)^(\s*ballastLiftPerTank\s*=\s*)[^\r\n]*', ('${1}' + $value)
    if ($updated -eq $text) { return }
    $backup = $file + '.before-dbv-balance'
    if (!(Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $file -Destination $backup }
} else { $updated = "[mechanics]`nballastLiftPerTank = $value`n" }
[IO.File]::WriteAllText($file, $updated, (New-Object System.Text.UTF8Encoding($false)))
