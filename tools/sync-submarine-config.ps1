param([string]$RuntimeDirectory)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (!$RuntimeDirectory) { $RuntimeDirectory = Join-Path $projectRoot 'runtime/client' }
$balance = Get-Content (Join-Path $projectRoot 'config/submarine-balance.json') -Raw | ConvertFrom-Json
$lift = [double]$balance.ballastLiftPerTank
$speed = [double]$balance.ballastVerticalSpeed
$floater = [double]$balance.floaterLift
if ([double]::IsNaN($lift) -or $lift -lt 1 -or $lift -gt 100000) { throw 'Invalid ballastLiftPerTank.' }
if ([double]::IsNaN($speed) -or $speed -lt 0.1 -or $speed -gt 20) { throw 'Invalid ballastVerticalSpeed.' }
if ([double]::IsNaN($floater) -or $floater -lt 1 -or $floater -gt 100000) { throw 'Invalid floaterLift.' }
$directory = Join-Path $RuntimeDirectory 'config'
New-Item -ItemType Directory -Force -Path $directory | Out-Null
$file = Join-Path $directory 'create_submarine-server.toml'
$value = $lift.ToString('0.0##', [Globalization.CultureInfo]::InvariantCulture)
$speedValue = $speed.ToString('0.0##', [Globalization.CultureInfo]::InvariantCulture)
$floaterValue = $floater.ToString('0.0##', [Globalization.CultureInfo]::InvariantCulture)
if (Test-Path -LiteralPath $file) {
    $text = [IO.File]::ReadAllText($file)
    if ($text -notmatch '(?m)^\s*ballastLiftPerTank\s*=') { throw 'Existing Deep Seas config has no ballastLiftPerTank; preserve it and inspect the version.' }
    $updated = $text -replace '(?m)^(\s*ballastLiftPerTank\s*=\s*)[^\r\n]*', ('${1}' + $value)
    if ($text -notmatch '(?m)^\s*ballastVerticalSpeed\s*=') { throw 'Existing Deep Seas config has no ballastVerticalSpeed.' }
    $updated = $updated -replace '(?m)^(\s*ballastVerticalSpeed\s*=\s*)[^\r\n]*', ('${1}' + $speedValue)
    if ($text -notmatch '(?m)^\s*floaterLift\s*=') { throw 'Existing Deep Seas config has no floaterLift.' }
    $updated = $updated -replace '(?m)^(\s*floaterLift\s*=\s*)[^\r\n]*', ('${1}' + $floaterValue)
    if ($updated -eq $text) { return }
    $backup = $file + '.before-dbv-balance'
    if (!(Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $file -Destination $backup }
} else { $updated = "[mechanics]`nballastLiftPerTank = $value`nballastVerticalSpeed = $speedValue`nfloaterLift = $floaterValue`n" }
[IO.File]::WriteAllText($file, $updated, (New-Object System.Text.UTF8Encoding($false)))
