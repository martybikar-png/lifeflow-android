# LifeFlow — Incident Response and Recovery Runbook

**Status:** Draft  
**Scope:** Android app + integrity trust boundary + emergency authority boundary  
**Purpose:** Operational response guide for security events before and after first production release

---

## 1. Operating principle

LifeFlow is fail-closed by design.

When compromise, tamper, invalid attestation, invalid trust verdict metadata,
or severe runtime security violation is detected, the system should prefer:

- containment before convenience
- evidence preservation before reset
- explicit recovery before silent continuation

---

## 2. Incident classes

## SEV-1 — Active compromise / containment required
Examples:

- trust state transitions to `COMPROMISED`
- tamper / repackaging detection
- runtime instrumentation / hooking detection
- invalid signature
- invalid attestation verification
- server verdict metadata invalid in release path
- protected runtime enters forced lockdown

Expected action:

- fail closed
- clear session
- block protected runtime
- require recovery path
- notify monitoring / operator review

---

## SEV-2 — Elevated abuse / high-risk instability
Examples:

- repeated auth failure bursts
- repeated policy violations
- repeated recovery failures
- unexpected trust degrade pattern
- emergency authority rejection bursts
- endpoint failures that do not yet prove compromise

Expected action:

- heightened guard
- throttle relevant paths
- review evidence
- decide whether trust degradation is needed
- monitor for escalation to SEV-1

---

## SEV-3 — Observe / warning activity
Examples:

- emulator warning in non-release evaluation context
- installer trust warning
- warning-only incident snapshot
- expected degraded but non-compromised state

Expected action:

- observe
- preserve telemetry
- do not overreact
- verify whether pattern is growing

---

## 3. Detection sources

Primary detection inputs currently available in the repository:

- `SecurityHardeningGuard`
- `SecurityTamperSignal`
- `RuntimeIntegrityCheck`
- `ApkSignatureVerifier`
- `SecurityIntegrityServerVerdictPolicy`
- `SecurityAuditLog`
- `SecurityAuditIncidentSignalAnalyzer`
- `SecurityIncidentResponseBridge`
- `SecurityRuntimeContainmentPolicy`
- `RuntimeSecuritySurveillanceCoordinator`

These sources together form the current incident signal chain:

**raw signal -> audit entry -> incident snapshot -> incident response snapshot -> containment posture**

---

## 4. Immediate response actions

## For SEV-1
1. Stop further protected operations.
2. Preserve current audit state.
3. Record release/build identity for the affected artifact.
4. Confirm whether the issue is reproducible.
5. Classify source:
   - tamper / runtime
   - integrity trust
   - auth/session
   - release/signing
   - endpoint / transport
6. Do not weaken checks to restore access.
7. Open recovery only through explicit controlled path.

## For SEV-2
1. Review last audit entries.
2. Confirm whether the incident is burst-based or single-source.
3. Check whether throttle decisions were expected.
4. Decide whether to degrade trust or keep observe/throttle.
5. Escalate to SEV-1 if compromise evidence appears.

## For SEV-3
1. Record the condition.
2. Watch for repetition.
3. Do not classify as compromise without evidence.
4. Keep evidence for future pattern comparison.

---

## 5. Evidence to preserve

Before clearing or rebuilding state, preserve:

- build variant
- version name / version code
- commit hash
- exact timestamp
- trust state at detection time
- incident level
- incident response mode
- containment codes
- recent audit entries
- latest critical event type
- recovery signal / rebuild source if present
- configured endpoint identifiers for the affected environment
- release artifact hash when applicable

Important rule:

**Do not log secrets, tokens, raw private key material, or PII.**

---

## 6. Recovery decision rules

## Allowed recovery
Recovery is allowed only when all are true:

- compromise source is understood well enough
- evidence has been preserved
- recovery path is explicit and auditable
- release protections are not being bypassed
- post-recovery validation is defined

## Not allowed recovery
Recovery must not continue when any is true:

- signing identity is uncertain
- APK signature mismatch is unexplained
- runtime tamper evidence remains active
- trust verdict metadata remains invalid
- server attestation verification remains invalid
- operator cannot distinguish genuine failure from active attack

---

## 7. Recovery paths

## Path A — Session-only recovery
Use when:

- compromise is not indicated
- session is stale / invalid / expired
- re-authentication is sufficient

Steps:

1. clear session
2. request fresh authentication
3. verify state returns to expected trust posture
4. confirm audit trail records the transition

## Path B — Protected runtime rebuild
Use when:

- runtime containment requires controlled rebuild
- startup or runtime security recovery controller requests rebuild
- one budgeted automatic rebuild is still allowed

Steps:

1. preserve audit snapshot
2. record rebuild source and recovery signal
3. close runtime safely
4. rebuild protected runtime
5. re-run validation gates
6. confirm no repeated rebuild loop occurs

## Path C — Vault reset authorization path
Use only when:

- explicit authorization exists
- reset is part of a deliberate recovery action
- the action is auditable and expected

Steps:

1. verify authorization grant
2. execute reset
3. verify new clean posture
4. verify session + binding + protected state re-entry rules

## Path D — No recovery / stop-ship
Use when:

- release artifact integrity is uncertain
- endpoint trust is uncertain
- signing trust is uncertain
- live compromise remains active

Steps:

1. freeze distribution
2. block release progression
3. escalate for operator review
4. prepare clean rebuild from trusted source

---

## 8. Post-incident validation

After any recovery action, re-check:

- trust state
- containment snapshot
- abuse monitoring snapshot
- protected runtime access behavior
- authentication flow
- vault reset guardrails
- integrity verdict path
- emergency authority connectivity assumptions
- release verification evidence when release-related

---

## 9. Closure criteria

An incident may be closed only when:

- root cause is documented
- containment was appropriate
- recovery was verified
- no active compromise signal remains
- release or runtime posture is back to approved state
- follow-up hardening items are recorded

---

## 10. Required follow-up outputs

Every meaningful incident should end with:

- short incident summary
- root cause
- impact statement
- containment summary
- recovery summary
- prevention items
- whether release readiness was affected

---

## 11. Non-negotiable rule

Never restore user convenience by weakening:

- trust-state enforcement
- signature verification
- attestation verification
- tamper detection
- release signing validation
- runtime containment decisions

If a fix needs weaker security to “work,” it is not a valid fix.