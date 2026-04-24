# DEXGUARD READY CHECKLIST

## Current verified baseline
- R8 release artifact verification exists and passes
- Selected security obfuscation verification exists and passes
- Release security baseline wrapper exists and passes
- Release R8 bundle export exists
- Release bundle manifest with SHA-256 exists
- Release bundle integrity verification exists
- Release ZIP archive exists
- Latest bundle pointer exists
- Manual and wrapper-based retrace workflow exists and works

## Verified security-sensitive areas already prepared
- KeyManager
- SecurityKeyAttestationBootstrap
- SecurityHardeningGuard
- PlayIntegrityVerifier
- GrpcEmergencyAuthorityTransport
- GrpcIntegrityTrustTransport
- IntegrityTrustTlsMaterialProvider
- EncryptionService

## Before DexGuard integration
- Keep current R8 baseline intact
- Do not add broad keep rules unless runtime evidence requires it
- Keep release mapping archive workflow operational
- Keep retrace workflow operational
- Preserve current release-security scripts under /tools
- Preserve archived bundle structure under /artifacts/release-r8

## When DexGuard becomes available
- Obtain exact plugin/manual/license instructions from Guardsquare customer portal
- Integrate DexGuard using official customer-portal instructions only
- Reuse existing R8/ProGuard baseline as starting point
- Re-run release archive workflow after DexGuard integration
- Re-run retrace workflow on the protected release output
- Compare protected build behavior against current baseline

## Project-specific caution
- Current project uses AGP 9.1.1
- Current project already depends on release mapping governance
- Do not replace current archive/retrace workflow with assumptions
- DexGuard integration must be validated against real build output, not only configuration text
