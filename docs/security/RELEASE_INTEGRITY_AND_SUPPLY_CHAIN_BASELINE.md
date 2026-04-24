# LifeFlow — Release Integrity and Supply Chain Baseline

**Status:** Draft  
**Scope:** Build, signing, dependency hygiene, release evidence, artifact integrity  
**Purpose:** Define the minimum trusted release path before external distribution

---

## 1. Release trust model

A release is trusted only if all layers below are trusted:

1. trusted source state
2. trusted dependencies
3. trusted build configuration
4. trusted signing identity
5. trusted release artifact
6. trusted runtime endpoint configuration
7. trusted retained evidence

If any layer is uncertain, release is **NO-GO**.

---

## 2. Trusted source baseline

Minimum source integrity rules:

- release must point to a frozen commit
- release must be reproducible from repository state
- no ad-hoc local edits after sign-off
- docs and release evidence must match the exact commit
- debug-only allowances must not leak into release logic

Recommended stored evidence:

- commit hash
- branch/tag name
- release timestamp
- operator identity
- changed files summary
- release notes reference

---

## 3. Dependency hygiene baseline

Before release:

- dependency versions must be explicit and intentional
- no unknown binary drops
- no placeholder runtime hosts
- no test-only transports in release path
- no debug signing assumptions in release path
- no disabled release security gates

Recommended release review points:

- Gradle version catalog diff
- Android Gradle Plugin version
- Kotlin version
- Play Integrity dependency version
- gRPC / transport dependency versions
- security-sensitive AndroidX versions

---

## 4. Signing integrity baseline

Release signing is part of the security boundary.

Required properties already enforced by the build:

- keystore file path
- store password
- key alias
- key password
- release signature SHA-256

Release must stop if:

- keystore file is missing
- signing properties are incomplete
- release certificate hash is missing
- configured release signature does not represent the intended signing identity

---

## 5. Artifact integrity baseline

For every release candidate, preserve:

- APK or AAB hash
- version name
- version code
- mapping.txt
- usage.txt
- seeds.txt
- configuration.txt
- release signature SHA-256
- endpoint configuration identifiers
- pinning configuration identifier

Required result:

- release obfuscation verification passes
- release artifact can be linked back to a single trusted commit
- evidence pack can be recreated later for audit / incident review

---

## 6. Runtime endpoint integrity baseline

Release runtime configuration must be production-valid.

Required runtime areas:

- integrity trust verdict host / port
- emergency authority control host / port
- emergency authority audit host / port
- Play Integrity cloud project number
- pinning enforcement choice
- pinned SPKI set when pinning is enabled

Release must stop if:

- any host is placeholder / invalid
- any port is invalid
- pinning is enabled without valid pins
- runtime environment is ambiguous

---

## 7. Release verification gates

Minimum release gate set:

- release build is explicitly allowed
- release signing properties are complete
- runtime security properties are complete
- release signature SHA-256 is valid
- R8 artifact verification passes
- selected security obfuscation verification passes
- debug compile gate is green
- androidTest compile gate is green
- targeted security instrumented verification is green

---

## 8. Manual release security checks

These still require explicit human validation:

## Device-side
- install release candidate to physical device
- verify biometric auth path
- verify session invalidation
- verify vault reset authorization path
- verify protected runtime lock behavior

## Network-side
- verify expected TLS behavior
- verify pinning behavior when enforced
- verify endpoint identity
- verify failure mode on transport denial

## Attack-side
- attempt repackaging validation
- attempt runtime instrumentation validation
- verify compromise results in fail-closed behavior

---

## 9. Supply-chain stop-ship rules

Release is **NO-GO** when any of the following is true:

- unknown dependency change
- unreviewed security-sensitive dependency bump
- release evidence pack incomplete
- signing identity unclear
- artifact hash not preserved
- endpoint configuration not frozen
- pinning configuration not understood
- independent verification prerequisites incomplete
- post-build artifact differs from expected signed output
- operator cannot prove what exactly was released

---

## 10. Minimum evidence pack

Store these together for each release candidate:

- release commit hash
- version metadata
- signed artifact hash
- release signature SHA-256
- mapping/usage/seeds/configuration
- security test result summary
- manual release security checklist result
- endpoint configuration identifiers
- pin-set identifier
- Go / No-Go decision record

---

## 11. Future hardening additions

This baseline should later grow with:

- provenance / signed build metadata
- SBOM generation
- dependency vulnerability review workflow
- release attestation
- stronger two-person release approval
- key rotation / signing continuity plan
- backend deployment integrity linkage

These are recommended next-phase enhancements, not yet claimed as complete.

---

## 12. Final rule

A release is not considered secure because it builds.

A release is considered security-ready only when:

- source is trusted
- signing is trusted
- artifact is verified
- runtime configuration is valid
- evidence is retained
- manual checks are completed
- the final decision is explicitly recorded