# DEXGUARD INTEGRATION ENVELOPE

## Purpose
This file defines the exact intake path for DexGuard delivery before any Gradle/project edits are made.

## Current verified baseline
- Pre-DexGuard release security archive workflow exists
- Latest R8 bundle archive exists
- Manifest + SHA-256 integrity workflow exists
- Retrace workflow exists and works
- Latest bundle pointer exists
- Release security toolkit exists and works

## Rule for DexGuard day
Do not edit Gradle or app configuration first.
First inspect the delivered DexGuard package and collect exact facts:
- customer manual location
- license file location
- Gradle/Maven integration clues
- plugin/package artifacts
- README/setup docs
- any version indicators in the delivery

## Then and only then
After the package is inspected:
1. identify exact integration path from delivered material
2. compare with current LifeFlow AGP/build structure
3. make the minimum clean project changes
4. rerun release security archive workflow
5. rerun crash resolution workflow against the new protected release output

## Expected outputs from the intake script
- delivery report text file
- matching file inventory
- likely documentation paths
- likely license paths
- likely Gradle/Maven/plugin artifact paths

## Notes
- Current project baseline is already protected by R8 archive governance
- DexGuard should be layered on top of the verified baseline
- No broad speculative config changes before reading the delivered instructions
