package com.lifeflow.security

internal const val SECURITY_ADVERSARIAL_SUITE_SESSION_MS = 30_000L

internal fun prepareVerifiedBaselineForSecuritySuite(reason: String) {
    SecurityRuleEngine.forceResetForAdversarialSuite(
        state = TrustState.VERIFIED,
        reason = reason
    )
    SecurityAccessSession.grant(SECURITY_ADVERSARIAL_SUITE_SESSION_MS)
}

internal fun prepareVerifiedNoSessionBaselineForSecuritySuite(reason: String) {
    SecurityRuleEngine.forceResetForAdversarialSuite(
        state = TrustState.VERIFIED,
        reason = reason
    )
    SecurityAccessSession.clear()
}

internal fun compromisedResultForSecuritySuite(
    name: String,
    successDetails: String
): SecurityAdversarialSuite.TestResult {
    val stateOk = SecurityRuleEngine.getTrustState() == TrustState.COMPROMISED
    val sessionOk = !SecurityAccessSession.isAuthorized()

    return if (stateOk && sessionOk) {
        SecurityAdversarialSuite.TestResult(name, true, successDetails)
    } else {
        SecurityAdversarialSuite.TestResult(
            name,
            false,
            "Mismatch: trustState=${SecurityRuleEngine.getTrustState()}, sessionAuthorized=${SecurityAccessSession.isAuthorized()}"
        )
    }
}