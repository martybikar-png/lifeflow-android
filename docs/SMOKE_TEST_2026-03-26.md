# LifeFlow Smoke Test Checklist

## Date: 2026-03-26
## Build: Phase 33 (commit 1eefb0a)
## Result: PASSED

---

## Test Scenarios

### 1. Cold Start
- [x] App spustí bez crash
- [x] Intro splash se zobrazí (~3s)
- [x] Loading screen se zobrazí po splash
- [x] Health Connect stav se načte správně

### 2. Authentication Flow
- [x] Tlačítko Authenticate je aktivní
- [x] Biometric prompt se zobrazí
- [x] Po úspěšné auth dashboard se zobrazí
- [x] lastAction tracking funguje

### 3. Protected Dashboard
- [x] Dashboard status karta se zobrazí
- [x] Health Connect karta se zobrazí
- [x] Digital Twin karta placeholder
- [x] Wellbeing Assessment karta placeholder
- [x] Recommended actions fungují

### 4. Health Connect States
- [x] State: Available
- [x] Permissions: 0/2 správně zobrazeno
- [x] Steps access: Not granted
- [x] Heart rate access: Not granted
- [x] Next move: Review health access

### 5. Settings/Privacy/Trust Shells
- [x] Settings shell navigace funguje
- [x] Privacy shell navigace funguje
- [x] Trust shell navigace funguje
- [x] Back navigation funguje

### 6. Fail-closed Behavior
- [x] Vault reset funguje
- [x] Recovery branch aktivní po reset
- [x] Error messages jsou jasné
- [x] Debug info je přehledné
