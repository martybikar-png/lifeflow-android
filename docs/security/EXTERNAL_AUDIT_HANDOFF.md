# LifeFlow — External Audit Handoff

## Status
Draft handoff package for independent mobile-app security review.

## Current internal state
LifeFlow currently has:
- internal trust-state engine
- fail-closed integrity verdict processing
- startup/runtime recovery and containment
- hardening checks for root/debugger/instrumentation/tamper/signature/runtime integrity
- audit / incident / abuse-monitoring snapshots
- release build guardrails in Gradle
- Android instrumented security test surface

This document does **not** claim external certification.
Approved wording after independent review should be similar to:
- "Independently assessed against OWASP MASVS"
- "Tested using MASVS/MASTG-aligned methodology"
Not:
- "OWASP certified"

## Review objective
Validate the Android client against a modern mobile-app security baseline and produce:
1. findings with severity and evidence
2. retest-ready reproduction steps
3. explicit pass/fail mapping to the selected review scope
4. independent recommendation for release readiness

## Review model
Preferred review mode:
- open-book review
- source code access
- release artifact access
- architecture/documentation access
- authenticated endpoint access where required
- at least one working account or equivalent runtime path for each relevant flow

## In-scope client areas
1. Authentication and session handling
2. Vault access / reset authorization / emergency access paths
3. Device binding
4. Android Keystore key posture and biometric requirements
5. Integrity trust request / response pipeline
6. Server verdict normalization and fail-closed rules
7. Certificate pinning / transport posture
8. Root / debugger / instrumentation / tamper / signature / runtime integrity checks
9. Runtime containment / lockdown / rebuild / recovery behavior
10. Audit logging / incident signal / abuse monitoring
11. Release obfuscation and release-security verification tasks
12. UI fail-closed behavior for protected flows

## Explicit out-of-scope unless added later
- backend ASVS review
- infrastructure / cloud account review
- SOC / SIEM implementation quality
- MDM / enterprise fleet controls
- DexGuard-specific controls
- iOS client

## Required artifact pack
Provide these items to the reviewer:
- exact Git commit SHA
- release candidate APK/AAB identifier
- signing certificate SHA-256
- mapping.txt
- usage.txt
- seeds.txt
- configuration.txt
- release ProGuard/R8 rules
- list of security-relevant Gradle properties used for release
- endpoint hostnames / ports for integrity trust and emergency authority
- certificate pinning mode and pinned SPKI set
- architecture notes for trust state / containment / recovery
- test command outputs for compile + androidTest compile
- known limitations and temporary exclusions

## Reviewer questions that must be answerable
- What transitions TrustState to DEGRADED or COMPROMISED?
- What can still run in DEGRADED?
- What causes immediate lockdown?
- What runtime rebuild budgets exist?
- How are replay / stale / request-binding failures handled?
- What evidence is written to audit logs?
- Which controls depend on backend correctness?
- Which release properties must be non-placeholder for production?

## Required evidence in the final independent report
The report should include:
- scope
- methodology
- tested artifact / commit ID
- tested environments
- findings with severity
- proof / evidence
- reproduction steps
- remediation guidance
- retest results
- residual risk summary

## Exit criteria for "externally reviewed"
All of the following must exist:
- independent written report
- explicit scope statement
- explicit tested artifact / commit
- evidence-backed findings
- remediation list
- retest section
- final release recommendation

## Internal note
LifeFlow should only be described as "externally reviewed" after the above package exists and the retest is complete.
