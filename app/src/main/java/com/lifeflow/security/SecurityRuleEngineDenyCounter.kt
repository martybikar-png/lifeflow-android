package com.lifeflow.security

internal class SecurityRuleEngineDenyCounter(
    val threshold: Int
) {
    init {
        require(threshold > 0) { "threshold must be > 0." }
    }

    private var count: Int = 0

    fun clear() {
        count = 0
    }

    fun incrementAndReachedThreshold(): Boolean {
        count += 1
        return count >= threshold
    }
}