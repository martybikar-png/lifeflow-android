[CmdletBinding()]
param(
    [string]$RepoRoot = 'C:\Users\marty\AndroidStudioProjects\LifeFlow'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Read-GradleProperties {
    param([string]$Path)

    $map = @{}
    if (-not (Test-Path $Path)) {
        return $map
    }

    foreach ($line in Get-Content $Path) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed)) { continue }
        if ($trimmed.StartsWith('#')) { continue }
        $index = $trimmed.IndexOf('=')
        if ($index -lt 1) { continue }

        $name = $trimmed.Substring(0, $index).Trim()
        $value = $trimmed.Substring($index + 1).Trim()
        if (-not [string]::IsNullOrWhiteSpace($name)) {
            $map[$name] = $value
        }
    }

    return $map
}

function Get-PropertyValue {
    param(
        [hashtable]$Map,
        [string]$Name
    )

    if ($Map.ContainsKey($Name)) {
        return [string]$Map[$Name]
    }

    return ''
}

function Test-PlaceholderHost {
    param([string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $true
    }

    return $Value.Trim().ToLowerInvariant().EndsWith('.invalid')
}

function Test-PortValue {
    param([string]$Value)

    $parsed = 0
    if (-not [int]::TryParse($Value, [ref]$parsed)) {
        return $false
    }

    return $parsed -ge 1 -and $parsed -le 65535
}

function Test-Sha256Hex {
    param([string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $false
    }

    return [bool]($Value.Trim().ToUpperInvariant() -match '^[0-9A-F]{64}$')
}

$projectProps = Read-GradleProperties (Join-Path $RepoRoot 'gradle.properties')
$userProps = Read-GradleProperties (Join-Path $env:USERPROFILE '.gradle\gradle.properties')

$props = @{}
foreach ($source in @($projectProps, $userProps)) {
    foreach ($entry in $source.GetEnumerator()) {
        $props[$entry.Key] = $entry.Value
    }
}

$requiredNames = @(
    'lifeflow.releaseStoreFile',
    'lifeflow.releaseStorePassword',
    'lifeflow.releaseKeyAlias',
    'lifeflow.releaseKeyPassword',
    'lifeflow.releaseSignatureSha256',
    'lifeflow.playIntegrityCloudProjectNumber',
    'lifeflow.integrityTrustVerdictHost',
    'lifeflow.integrityTrustVerdictPort',
    'lifeflow.emergencyAuthorityControlHost',
    'lifeflow.emergencyAuthorityControlPort',
    'lifeflow.emergencyAuthorityAuditHost',
    'lifeflow.emergencyAuthorityAuditPort'
)

$missing = New-Object System.Collections.Generic.List[string]
$invalid = New-Object System.Collections.Generic.List[string]

foreach ($name in $requiredNames) {
    $value = Get-PropertyValue -Map $props -Name $name
    if ([string]::IsNullOrWhiteSpace($value)) {
        $missing.Add($name)
    }
}

$releaseStoreFile = Get-PropertyValue -Map $props -Name 'lifeflow.releaseStoreFile'
if (-not [string]::IsNullOrWhiteSpace($releaseStoreFile) -and -not (Test-Path $releaseStoreFile)) {
    $invalid.Add('lifeflow.releaseStoreFile (file not found)')
}

$releaseSignatureSha256 = Get-PropertyValue -Map $props -Name 'lifeflow.releaseSignatureSha256'
if (-not [string]::IsNullOrWhiteSpace($releaseSignatureSha256) -and -not (Test-Sha256Hex $releaseSignatureSha256)) {
    $invalid.Add('lifeflow.releaseSignatureSha256 (must be 64 hex chars)')
}

$cloudProjectNumber = Get-PropertyValue -Map $props -Name 'lifeflow.playIntegrityCloudProjectNumber'
$cloudProjectParsed = 0L
if (-not [string]::IsNullOrWhiteSpace($cloudProjectNumber)) {
    if (-not [long]::TryParse($cloudProjectNumber, [ref]$cloudProjectParsed) -or $cloudProjectParsed -le 0) {
        $invalid.Add('lifeflow.playIntegrityCloudProjectNumber (must be positive integer)')
    }
}

$hostNames = @(
    'lifeflow.integrityTrustVerdictHost',
    'lifeflow.emergencyAuthorityControlHost',
    'lifeflow.emergencyAuthorityAuditHost'
)

foreach ($name in $hostNames) {
    $value = Get-PropertyValue -Map $props -Name $name
    if (-not [string]::IsNullOrWhiteSpace($value) -and (Test-PlaceholderHost $value)) {
        $invalid.Add("$name (placeholder host)")
    }
}

$portNames = @(
    'lifeflow.integrityTrustVerdictPort',
    'lifeflow.emergencyAuthorityControlPort',
    'lifeflow.emergencyAuthorityAuditPort'
)

foreach ($name in $portNames) {
    $value = Get-PropertyValue -Map $props -Name $name
    if (-not [string]::IsNullOrWhiteSpace($value) -and -not (Test-PortValue $value)) {
        $invalid.Add("$name (must be 1..65535)")
    }
}

$pinningEnforced = (Get-PropertyValue -Map $props -Name 'lifeflow.integrityTrustVerdictPinningEnforced').Trim().ToLowerInvariant()
$pinnedSet = Get-PropertyValue -Map $props -Name 'lifeflow.integrityTrustVerdictPinnedSpkiSha256Set'
if ($pinningEnforced -eq 'true' -and [string]::IsNullOrWhiteSpace($pinnedSet)) {
    $invalid.Add('lifeflow.integrityTrustVerdictPinnedSpkiSha256Set (required when pinning enforced)')
}

Write-Host ""
Write-Host '=== RELEASE SECURITY PREFLIGHT ==='
Write-Host "RepoRoot: $RepoRoot"
Write-Host "Project gradle.properties: $(Join-Path $RepoRoot 'gradle.properties')"
Write-Host "User gradle.properties: $(Join-Path $env:USERPROFILE '.gradle\gradle.properties')"
Write-Host ""

if ($missing.Count -eq 0) {
    Write-Host 'Missing required properties: none'
} else {
    Write-Host 'Missing required properties:'
    $missing | ForEach-Object { Write-Host " - $_" }
}

Write-Host ""
if ($invalid.Count -eq 0) {
    Write-Host 'Invalid properties: none'
} else {
    Write-Host 'Invalid properties:'
    $invalid | ForEach-Object { Write-Host " - $_" }
}

Write-Host ""
if ($missing.Count -eq 0 -and $invalid.Count -eq 0) {
    Write-Host 'RESULT: READY FOR REAL RELEASE EVIDENCE CAPTURE'
    exit 0
}

Write-Host 'RESULT: NOT READY FOR REAL RELEASE EVIDENCE CAPTURE'
exit 1