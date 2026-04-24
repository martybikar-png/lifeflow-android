package com.lifeflow.core

/**
 * VERSIONED ARCHITECTURE LEDGER — Phase VI
 *
 * Immutable record of architectural decisions and milestones.
 * Append-only. Never modify existing entries.
 */
object LifeFlowArchitectureLedger {

    val entries: List<LedgerEntry> = listOf(

        LedgerEntry(
            version = "0.1.0",
            date = "2026-02-28",
            phase = "I",
            title = "Core Skeleton",
            description = "LifeFlowOrchestrator established as single entrypoint. " +
                    "ActionResult pattern introduced. Identity bootstrap fail-closed."
        ),

        LedgerEntry(
            version = "0.2.0",
            date = "2026-03-04",
            phase = "II",
            title = "Identity & Security",
            description = "BiometricAuthManager BIOMETRIC_STRONG only. " +
                    "EncryptionService AES-256-GCM versioned format. " +
                    "AndroidDataSovereigntyVault monotonic versioning."
        ),

        LedgerEntry(
            version = "0.3.0",
            date = "2026-03-16",
            phase = "III",
            title = "Rule Engine",
            description = "SecurityRuleEngine deny-by-default. " +
                    "Automatic trust transitions: VERIFIED -> DEGRADED -> COMPROMISED. " +
                    "recoverAfterVaultReset as only production recovery path."
        ),

        LedgerEntry(
            version = "0.4.0",
            date = "2026-03-17",
            phase = "III-IV",
            title = "UI State Flow",
            description = "UiState: Loading / Authenticated / Error. " +
                    "ErrorScreen napojený na errorGuidanceMessage. " +
                    "resetRequired propaguje přes router do ErrorScreen."
        ),

        LedgerEntry(
            version = "0.5.0",
            date = "2026-03-18",
            phase = "IV",
            title = "Module Layer",
            description = "8 domain modulů: HolisticWellbeingNode, AdaptiveTimeline, " +
                    "AutonomousHabits, ShadowDiary, QuantumInsights, SecondBrain, " +
                    "PredictiveShopping, IntimacyConnection. " +
                    "Všechny V1 rule-based. All verification moved to instrumented/device coverage."
        ),

        LedgerEntry(
            version = "0.6.0",
            date = "2026-03-18",
            phase = "V",
            title = "Master Lock",
            description = "LifeFlowMasterOrchestrationMode.verify() == LOCKED. " +
                    "8 modulů registrováno. Authority chain uzavřen."
        ),

        LedgerEntry(
            version = "0.7.0",
            date = "2026-03-18",
            phase = "VI",
            title = "Release Freeze",
            description = "FreezeChecklist 33 položek. SignOffProtocol 26 gates. " +
                    "Architecture Ledger uzavřen. " +
                    "Stack: :app:compileDebugKotlin + :app:compileDebugAndroidTestKotlin SUCCESSFUL."
        )
    )

    fun latest(): LedgerEntry = entries.last()

    fun summary(): String {
        return "LifeFlow One — Architecture Ledger — ${entries.size} entries — " +
                "latest: ${latest().version} (Phase ${latest().phase})"
    }
}

data class LedgerEntry(
    val version: String,
    val date: String,
    val phase: String,
    val title: String,
    val description: String
)

