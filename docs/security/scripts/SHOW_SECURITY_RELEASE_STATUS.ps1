[CmdletBinding()]
param(
    [string]$RepoRoot = 'C:\Users\marty\AndroidStudioProjects\LifeFlow'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$docs = Join-Path $RepoRoot 'docs\security'
$zipPath = Join-Path $docs 'LifeFlow_External_Assessment_Pack.zip'

$canonical = @(
    'README.md',
    'SECURITY_FINAL_READINESS_MASTER.md',
    'RELEASE_SECURITY_READINESS.md',
    'PRODUCTION_RELEASE_EVIDENCE.md',
    'EXTERNAL_AUDIT_SCOPE.md',
    'EXTERNAL_ASSESSOR_HANDOFF.md',
    'INDEPENDENT_VERIFICATION_MATRIX.md',
    'FINDINGS_RETEST_REGISTER.md',
    'OPERATIONAL_SECURITY_READINESS.md',
    'RELEASE_SECURITY_SIGNOFF.md'
)

$compileStatus = 'UNKNOWN'
try {
    $gradlew = Join-Path $RepoRoot 'gradlew.bat'
    if (-not (Test-Path $gradlew)) {
        throw "Missing Gradle wrapper: $gradlew"
    }

    & $gradlew ':app:compileDebugKotlin' ':app:compileDebugAndroidTestKotlin' | Out-Null
    if ($LASTEXITCODE -eq 0) {
        $compileStatus = 'PASS'
    } else {
        $compileStatus = 'FAIL'
    }
} catch {
    $compileStatus = 'FAIL'
}

Write-Host ""
Write-Host '=== LIFEFLOW SECURITY RELEASE STATUS ==='
Write-Host "RepoRoot: $RepoRoot"
Write-Host "Docs: $docs"
Write-Host ""
Write-Host 'Current truth:'
Write-Host " - internal compile state: $compileStatus"
Write-Host ' - internal security core: READY FOR EXTERNAL ASSESSMENT'
Write-Host ' - production artifact evidence: NOT YET CAPTURED'
Write-Host ' - external independent validation: NOT YET COMPLETED'
Write-Host ' - findings and retest closure: NOT YET COMPLETED'
Write-Host ' - final release sign-off: NOT YET COMPLETED'
Write-Host ""
Write-Host 'Correct current decision:'
Write-Host ' - READY FOR EXTERNAL ASSESSMENT'
Write-Host ' - HOLD FOR PRODUCTION RELEASE'
Write-Host ""
Write-Host 'Canonical files:'
foreach ($name in $canonical) {
    $filePath = Join-Path $docs $name
    $state = if (Test-Path $filePath) { 'OK' } else { 'MISSING' }
    Write-Host (" - [{0}] {1}" -f $state, $filePath)
}
Write-Host ""
if (Test-Path $zipPath) {
    $zip = Get-Item $zipPath
    Write-Host 'External assessment pack:'
    Write-Host (" - OK {0} ({1} bytes, {2})" -f $zip.FullName, $zip.Length, $zip.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss'))
} else {
    Write-Host 'External assessment pack:'
    Write-Host (" - MISSING {0}" -f $zipPath)
}
Write-Host ""
Write-Host 'Next allowed actions:'
Write-Host ' 1. Rebuild external assessment pack'
Write-Host ' 2. Run release preflight when real production inputs are ready'
Write-Host ' 3. Run production evidence capture only for a real release candidate'
