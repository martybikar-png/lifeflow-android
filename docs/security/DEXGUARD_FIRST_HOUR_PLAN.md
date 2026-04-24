# DEXGUARD FIRST HOUR PLAN

## Goal
Use the real delivered DexGuard package to create a fact-based integration workspace before changing Gradle or project configuration.

## Input sources
- artifacts/dexguard-drop
- artifacts/dexguard-intake
- artifacts/dexguard-baseline
- artifacts/release-r8/LATEST_BUNDLE.txt

## First-hour order
1. Confirm DexGuard delivery is physically present in dexguard-drop
2. Run Start-DexGuardDay.ps1
3. Read the newest intake report and inventory
4. Build a session workspace with links/copies to:
   - latest intake report
   - latest intake inventory
   - latest DexGuard baseline
   - latest R8 bundle
   - latest bundle manifest
5. Read project AGP version and current release-security docs
6. Only after that decide exact Gradle/plugin/repository edits from the delivered instructions

## Rules
- No speculative DexGuard plugin syntax
- No broad config rewrite before reading delivered docs
- Preserve current R8 archive/retrace workflow
- Preserve the baseline restore point

## Immediate next action after workspace creation
Use the session workspace as the single source of truth for day-one DexGuard edits.
