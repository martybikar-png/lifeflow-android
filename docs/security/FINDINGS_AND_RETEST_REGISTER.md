# FINDINGS AND RETEST REGISTER - LIFEFLOW

Status: Draft
Owner: Security / Release

## 1. Purpose
Track all external and internal security findings and their remediation / retest state.

## 2. Status Values
Use:
- OPEN
- FIX IN PROGRESS
- READY FOR RETEST
- RETEST PASSED
- RETEST FAILED
- ACCEPTED EXCEPTION
- CLOSED

## 3. Severity Values
Use:
- CRITICAL
- HIGH
- MEDIUM
- LOW
- INFO

## 4. Register
| Finding ID | Source | Severity | Title | Status | Commit | Candidate | Retest Required | Retest Result | Notes |
|---|---|---|---|---|---|---|---|---|---|
| TBD | TBD | TBD | TBD | OPEN | - | - | TBD | - | - |

## 5. Required Per Finding
Every real finding should record:
- source
- severity
- affected area
- short description
- remediation commit
- target release candidate
- retest requirement
- closure evidence

## 6. Blocking Rule
CRITICAL and HIGH findings block release until:
- fixed, and
- retested, or
- explicitly accepted as an exception by sign-off owners

## 7. Retest Evidence
When retest happens, add:
- retest date
- retest by
- pass/fail
- residual risk
- closure recommendation
