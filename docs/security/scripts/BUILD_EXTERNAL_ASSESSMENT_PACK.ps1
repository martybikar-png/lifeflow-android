$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$securityDir = Resolve-Path (Join-Path $scriptDir "..")
$repoRoot = Resolve-Path (Join-Path $securityDir "..\..")

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputDir = Join-Path $securityDir "external-assessment-pack"
$stageDir = Join-Path $env:TEMP ("lifeflow-external-assessment-pack-" + $timestamp)
$zipPath = Join-Path $outputDir ("LifeFlow-ExternalAssessmentPack-" + $timestamp + ".zip")

New-Item -ItemType Directory -Force $outputDir | Out-Null
New-Item -ItemType Directory -Force $stageDir | Out-Null

try {
    $stageSecurityDir = Join-Path $stageDir "docs\security"
    New-Item -ItemType Directory -Force $stageSecurityDir | Out-Null

    Get-ChildItem -Path $securityDir -Recurse -File |
        Where-Object {
            $_.FullName -notlike (Join-Path $outputDir "*") -and
            $_.Extension -ne ".zip" -and
            $_.Name -notlike "*.bak" -and
            $_.Name -notlike "*.tmp"
        } |
        ForEach-Object {
            $relativePath = [System.IO.Path]::GetRelativePath($securityDir, $_.FullName)
            $targetPath = Join-Path $stageSecurityDir $relativePath
            $targetParent = Split-Path -Parent $targetPath
            New-Item -ItemType Directory -Force $targetParent | Out-Null
            Copy-Item -Path $_.FullName -Destination $targetPath -Force
        }

    if (Test-Path $zipPath) {
        Remove-Item $zipPath -Force
    }

    Compress-Archive -Path (Join-Path $stageDir "*") -DestinationPath $zipPath -Force

    Write-Host ""
    Write-Host "=== EXTERNAL ASSESSMENT PACK CREATED ==="
    Write-Host $zipPath
    Write-Host ""
}
finally {
    if (Test-Path $stageDir) {
        Remove-Item $stageDir -Recurse -Force
    }
}
