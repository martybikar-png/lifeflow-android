# PRODUCTION_RELEASE_EVIDENCE

Status: OPEN UNTIL REAL PRODUCTION CANDIDATE EXISTS
Project: LifeFlow
Last updated: 2026-04-24

## 1. Purpose

Tento dokument je kanonický production release evidence record.

Musí dokazovat:
- který přesný release candidate je posuzovaný
- který přesný artefakt byl vytvořen
- která signing identity byla použitá
- které release-security kontroly prošly
- jaký evidence pack podporuje release decision

## 2. Current truth

Aktuální repository state ještě není production-proven.

Aktuální pravda je:
- internal compile state: PASS
- internal security core readiness: PASS
- production artifact evidence: NOT YET CAPTURED
- external validation: NOT YET COMPLETED
- final sign-off: NOT YET COMPLETED

Proto tento dokument zůstává OPEN, dokud neexistuje reálný production candidate.

## 3. Use this document only when all of the following are true

- frozen release candidate exists
- real release artifact exists
- release signing identity exists for that artifact
- release security baseline tasks were actually run
- captured outputs can be attached as evidence

## 4. Required release prerequisites

Release candidate nesmí být považovaný za evidence-ready, dokud nejsou nastavené tyto vstupy:

- lifeflow.allowReleaseBuild=true
- lifeflow.releaseStoreFile
- lifeflow.releaseStorePassword
- lifeflow.releaseKeyAlias
- lifeflow.releaseKeyPassword
- lifeflow.releaseSignatureSha256
- lifeflow.playIntegrityCloudProjectNumber
- lifeflow.integrityTrustVerdictHost
- lifeflow.integrityTrustVerdictPort
- lifeflow.emergencyAuthorityControlHost
- lifeflow.emergencyAuthorityControlPort
- lifeflow.emergencyAuthorityAuditHost
- lifeflow.emergencyAuthorityAuditPort

Optional by policy:
- lifeflow.integrityTrustVerdictPinningEnforced
- lifeflow.integrityTrustVerdictPinnedSpkiSha256Set

## 5. Required release commands

Run only after secure release inputs are ready:

- Release baseline command:
  .\gradlew :app:bundleRelease :app:verifyReleaseSecurityBaseline -Plifeflow.allowReleaseBuild=true

- Internal safety command:
  .\gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin

## 6. Minimum evidence fields

- Release candidate ID
- Version name
- Version code
- Commit hash
- Tag
- Artifact path
- Artifact SHA-256
- Signing SHA-256
- Build timestamp
- Verification output references

## 7. Final rule

Production release evidence je validní až tehdy, když je navázaná na skutečný artifact, skutečný signing fingerprint a skutečný verification output.