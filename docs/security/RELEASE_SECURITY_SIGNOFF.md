# RELEASE_SECURITY_SIGNOFF

Status: FINAL DECISION TEMPLATE
Project: LifeFlow
Last updated: 2026-04-24

## 1. Purpose

Toto je finální release security decision record.

## 2. Current default decision

Dokud neexistuje production artifact evidence, external validation, findings/retest closure a finální approvals, správný default výsledek je:

- HOLD

## 3. Required inputs

Sign-off musí odkazovat na:
- release candidate ID
- commit hash
- artifact SHA-256
- signing SHA-256
- production release evidence
- external assessment result
- findings register state
- retest state
- operational readiness state

## 4. Decision values

Používej jen:
- GO
- HOLD
- NO-GO

## 5. Required approvers

- Security owner
- Technical owner
- Release owner

## 6. GO rule

GO je možné pouze když současně platí:

- real production artifact evidence exists
- external validation is completed
- release-blocking findings are closed or explicitly accepted
- retest is completed where required
- all required owners approved the decision

## 7. HOLD rule

HOLD je správně když chybí alespoň jedna z těchto oblastí:

- artifact evidence
- external validation
- findings closure
- retest closure
- operational ownership
- final approvals

## 8. Final rule

Until then, the correct default result is HOLD.