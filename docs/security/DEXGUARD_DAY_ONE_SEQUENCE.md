# DEXGUARD DAY ONE SEQUENCE

## Goal
Integrate DexGuard only after inspecting the real delivered package and only on top of the already verified R8 baseline.

## Before changing project files
1. Put the real DexGuard delivery into:
   C:\Users\marty\AndroidStudioProjects\LifeFlow\artifacts\dexguard-drop
2. Run:
   powershell -ExecutionPolicy Bypass -File "C:\Users\marty\AndroidStudioProjects\LifeFlow\tools\Start-DexGuardDay.ps1"
3. Read the generated intake report and inventory.
4. Confirm exact delivered docs, plugin artifacts, license files and setup instructions.

## Current safe restore point
A DexGuard baseline snapshot exists under:
artifacts\dexguard-baseline\TIMESTAMP

If integration goes wrong, restore from:
Restore-DexGuardBaseline.ps1

## Files most likely to change on DexGuard day
- settings.gradle.kts
- build.gradle.kts
- gradle/libs.versions.toml
- gradle.properties
- app/build.gradle.kts
- app/proguard-rules.pro
- module proguard-rules files if required by real delivered instructions

## First wave after real delivery inspection
1. Apply only the minimum plugin/repository changes required by the delivered docs.
2. Apply only the minimum DexGuard config entry points required by the delivered docs.
3. Do not remove the current release archive / retrace workflow.
4. Re-run:
   powershell -ExecutionPolicy Bypass -File "C:\Users\marty\AndroidStudioProjects\LifeFlow\tools\Invoke-ReleaseSecurityToolkit.ps1" -Mode archive
5. Re-run crash resolution on a known sample stack trace.
6. Only then continue to stronger DexGuard policy tuning.

## Rule
No speculative DexGuard syntax before reading the actual delivered material.
