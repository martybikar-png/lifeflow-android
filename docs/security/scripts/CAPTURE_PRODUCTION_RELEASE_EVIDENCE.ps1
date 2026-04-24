[CmdletBinding()]
param(
    [string]$RepoRoot = 'C:\Users\marty\AndroidStudioProjects\LifeFlow',
    [string]$CandidateId = '',
    [switch]$SkipPreflight
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$docs = Join-Path $RepoRoot 'docs\security'
$scriptsDir = Join-Path $docs 'scripts'
$preflightPath = Join-Path $scriptsDir 'CHECK_RELEASE_PREREQUISITES.ps1'
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if (-not $SkipPreflight) {
    if (-not (Test-Path $preflightPath)) {
        throw "Preflight script not found: $preflightPath"
    }

    & pwsh -NoProfile -ExecutionPolicy Bypass -File $preflightPath -RepoRoot $RepoRoot
    if ($LASTEXITCODE -ne 0) {
        throw 'Release preflight failed. Fix missing or invalid properties first.'
    }
}

Push-Location $RepoRoot
try {
    $timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
    if ([string]::IsNullOrWhiteSpace($CandidateId)) {
        $CandidateId = "lifeflow_rc_$timestamp"
    }

    $evidenceDir = Join-Path $docs ("release_evidence\" + $CandidateId)
    New-Item -ItemType Directory -Force $evidenceDir | Out-Null

    $gradleOutputPath = Join-Path $evidenceDir 'gradle_release_output.txt'
    $summaryPath = Join-Path $evidenceDir 'PRODUCTION_RELEASE_EVIDENCE_CAPTURED.md'

    $gradleArgs = @(
        ':app:bundleRelease',
        ':app:verifyReleaseSecurityBaseline',
        '-Plifeflow.allowReleaseBuild=true'
    )

    & .\gradlew @gradleArgs 2>&1 | Tee-Object -FilePath $gradleOutputPath
    if ($LASTEXITCODE -ne 0) {
        throw 'Release build or baseline verification failed.'
    }

    $artifact = Get-ChildItem (Join-Path $RepoRoot 'app\build\outputs\bundle\release') -Filter *.aab -File |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $artifact) {
        throw 'Could not find a release AAB artifact in app\build\outputs\bundle\release.'
    }

    $artifactHash = (Get-FileHash $artifact.FullName -Algorithm SHA256).Hash

    $commitHash = 'UNKNOWN'
    try {
        $commitHash = (& git rev-parse HEAD).Trim()
        if (-not $commitHash) { $commitHash = 'UNKNOWN' }
    } catch {
        $commitHash = 'UNKNOWN'
    }

    $tag = ''
    try {
        $tag = (& git tag --points-at HEAD | Select-Object -First 1)
        if ($null -eq $tag) { $tag = '' }
    } catch {
        $tag = ''
    }

    $lines = @(
        '# PRODUCTION_RELEASE_EVIDENCE_CAPTURED',
        '',
        'Status: CAPTURED FROM REAL RELEASE TASKS',
        "Candidate ID: $CandidateId",
        "Captured at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')",
        '',
        '## Artifact',
        "Artifact path: $($artifact.FullName)",
        "Artifact SHA-256: $artifactHash",
        "Artifact last write time: $($artifact.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss'))",
        '',
        '## Source identity',
        "Commit hash: $commitHash",
        "Tag: $tag",
        '',
        '## Verification outputs',
        "Gradle output: $gradleOutputPath",
        '',
        '## Rule',
        'Attach this evidence to PRODUCTION_RELEASE_EVIDENCE.md and RELEASE_SECURITY_SIGNOFF.md before any production decision.'
    )

    [System.IO.File]::WriteAllText($summaryPath, ($lines -join "`r`n"), $utf8NoBom)

    Write-Host ""
    Write-Host '=== RELEASE EVIDENCE CAPTURED ==='
    Write-Host "Candidate ID: $CandidateId"
    Write-Host "Artifact path: $($artifact.FullName)"
    Write-Host "Artifact SHA-256: $artifactHash"
    Write-Host "Summary path: $summaryPath"
    Write-Host "Gradle output path: $gradleOutputPath"
}
finally {
    Pop-Location
}