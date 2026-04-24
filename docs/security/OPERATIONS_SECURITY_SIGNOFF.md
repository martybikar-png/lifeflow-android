# OPERATIONS SECURITY SIGNOFF - LIFEFLOW

Status: Draft
Owner: Operations / Security / Release

## 1. Purpose
Record the operational readiness decision for a release candidate after security review.

## 2. Required Inputs
- release candidate ID
- commit hash
- production release evidence
- external assessment status
- findings register status
- retest status
- rollback owner
- incident owner
- release owner

## 3. Operations Questions
Before sign-off, operations should confirm:
- do we know which candidate is being released?
- do we know how to identify that artifact later?
- do we have rollback ownership?
- do we have incident ownership?
- do we know whether any open findings remain?
- do we know whether external retest is still pending?

## 4. Sign-Off Record
- Candidate:
- Commit:
- Release owner:
- Security owner:
- Operations owner:
- Rollback owner:
- Incident owner:
- External assessment status:
- Findings status:
- Retest status:
- Decision:
- Date:
- Notes:

## 5. Decision Values
Use:
- BLOCKED
- READY FOR CONTROLLED RELEASE
- READY FOR PRODUCTION RELEASE
- READY PENDING RETEST
- READY WITH EXCEPTION

## 6. Rule
Operational release approval is incomplete until ownership is explicit and the decision is recorded here.
