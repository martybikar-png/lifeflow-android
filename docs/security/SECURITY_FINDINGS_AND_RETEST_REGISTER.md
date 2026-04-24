# SECURITY_FINDINGS_AND_RETEST_REGISTER

Status: TRACKING TEMPLATE  
Project: LifeFlow  
Purpose: one register for findings, remediation, retest, and release blocking status

## 1. Usage rule

Every externally reported or internally release-relevant security finding must be tracked here.

## 2. Severity model

Use one:
- Critical
- High
- Medium
- Low
- Informational

## 3. Release blocking rule

Default:
- Critical = blocking
- High = blocking
- Medium = blocking unless explicitly accepted
- Low = non-blocking unless chained into a larger issue
- Informational = non-blocking

## 4. Findings table

| ID | Source | Title | Severity | Release blocking | Status | Owner | Fix version | Retest required | Retest status | Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| F-001 | external / internal |  |  | yes / no | open / fixing / fixed / accepted / retested |  |  | yes / no | pending / passed / failed |  |

## 5. Required detail per finding

For each finding also record:
- exact affected area
- reproduction summary
- security impact
- remediation summary
- commit / branch reference
- evidence reference
- retest reference

## 6. Status meanings

- open = confirmed and not yet fixed
- fixing = remediation in progress
- fixed = remediation implemented, awaiting retest or verification
- accepted = explicitly accepted residual risk
- retested = independently rechecked after remediation

## 7. Acceptance rule

A release-blocking finding must not move to releasable status unless:
- it is fixed and retested, or
- it has a formally approved risk acceptance.

## 8. Final rule

This register is the source of truth for:
- what is still wrong,
- what was fixed,
- what was retested,
- what still blocks release.
