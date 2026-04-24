# INDEPENDENT VERIFICATION READINESS

Status: INTERNAL BASELINE READY / EXTERNAL VALIDATION PENDING  
System: LifeFlow Android app  
Scope: mobile app core, protected runtime, trust boundary, vault, auth, release gating

## 1. Purpose

Tento dokument připravuje LifeFlow na:
- nezávislé bezpečnostní ověření,
- externí mobilní security assessment / pen test,
- retest po nálezech,
- finální release security sign-off.

Neřeší marketing ani UI polish. Řeší pouze security readiness a důkazní vrstvu.

## 2. Current internal baseline

Aktuální interní baseline je považována za připravenou k externímu ověření, pokud současně platí:

- debug compile gate je zelený,
- androidTest compile gate je zelený,
- release build je blokovaný defaultně a jde spustit jen explicitně,
- release signing a runtime security properties mají gate v build procesu,
- integrity trust flow obsahuje:
  - request hash binding,
  - server freshness / skew kontrolu,
  - replay ochranu přes requestHashEcho,
  - attestation verification fail-closed,
  - zero-trust decision mapping,
- runtime security vrstva obsahuje:
  - hardening checks,
  - tamper / instrumentation signal detekci,
  - trust-state monitoring,
  - controlled protected-runtime rebuild flow,
  - audit -> incident -> response -> containment pipeline,
- auth / vault / emergency / reset flow mají auditované bezpečnostní přechody,
- existují instrumented security testy a adversarial coverage.

## 3. What must be handed to the external assessor

Externímu hodnotiteli musí být předáno minimum:

1. release candidate APK/AAB určený pro test,
2. mapa architektury security boundary,
3. seznam chráněných toků:
   - auth,
   - session,
   - vault reset,
   - device binding,
   - integrity trust,
   - runtime containment,
   - emergency authority boundary,
4. seznam záměrně mimo scope,
5. build / signing / release gate pravidla,
6. interní známá omezení,
7. očekávaný způsob reportingu nálezů.

## 4. External assessment scope

Externí ověření má minimálně pokrýt:

- storage of sensitive data,
- cryptographic posture and key usage,
- local authentication and sensitive-operation protection,
- network trust and transport hardening,
- platform interaction and Android-specific boundaries,
- code / build / release hardening posture,
- resilience against tampering / repackaging / runtime instrumentation,
- privacy-sensitive data handling where relevant for the current release.

## 5. Explicit current exclusions

Mimo aktuální scope, pokud nebudou předány zvlášť:

- plný backend production environment review,
- cloud infrastructure review,
- SOC / SIEM integration validation,
- organizational incident response tabletop exercise,
- vendor-specific commercial hardening layers, které v aktuální větvi nejsou zapnuté.

## 6. Entry criteria for independent verification

Externí ověření smí začít až když platí vše níže:

- BUILD SUCCESSFUL pro:
  - :app:compileDebugKotlin
  - :app:compileDebugAndroidTestKotlin
- release gating pravidla jsou v repu přítomná,
- security docs v docs/security jsou aktuální,
- test build je jednoznačně označený,
- assessor dostal přesný scope a expected deliverables,
- je určeno, kdo rozhoduje o:
  - accepted risk,
  - mandatory fix,
  - retest closure,
  - release go/no-go.

## 7. Expected deliverables from the external assessor

Po externím ověření musí existovat:

- executive summary,
- technical findings list,
- severity per finding,
- reproduction steps,
- impacted component / boundary,
- recommendation,
- evidence,
- final residual risk statement.

Bez těchto výstupů se ověření nepovažuje za uzavřené.

## 8. Retest rule

Každý nález označený jako:
- Critical,
- High,
- nebo release-blocking Medium

musí projít samostatným retestem.

Retest musí potvrdit:
- původní issue už není reprodukovatelná,
- nevznikl bypass,
- nevznikla regresní slabina v sousední vrstvě,
- build / release posture zůstala konzistentní.

## 9. Exit criteria

Tato fáze je uzavřena až když současně platí:

- interní baseline je zelená,
- externí assessment byl doručen,
- release-blocking findings jsou opravené,
- proběhl retest,
- zůstávající risk byl výslovně přijat,
- security sign-off je vyplněný,
- release decision je GO nebo HOLD s jasným důvodem.

## 10. Release interpretation

Dokud chybí:
- reálný produkční release důkaz,
- externí nezávislé ověření,
- retest po nálezech,
- operační / security sign-off,

nelze systém označit za finálně produkčně bezpečnostně uzavřený.

Lze ho ale označit jako:
INTERNALLY HARDENED / PRE-EXTERNAL-VALIDATION READY.
