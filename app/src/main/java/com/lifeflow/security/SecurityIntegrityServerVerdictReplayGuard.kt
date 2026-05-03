package com.lifeflow.security

internal class SecurityIntegrityServerVerdictReplayGuard(
    private val maxConsumedRequestHashes: Int
) {
    init {
        require(maxConsumedRequestHashes > 0) {
            "maxConsumedRequestHashes must be > 0."
        }
    }

    private val consumedServerVerdictRequestHashes = linkedSetOf<String>()

    fun clear() {
        consumedServerVerdictRequestHashes.clear()
    }

    fun remember(
        requestHashEcho: String
    ): Boolean {
        if (requestHashEcho in consumedServerVerdictRequestHashes) {
            return false
        }

        while (consumedServerVerdictRequestHashes.size >= maxConsumedRequestHashes) {
            val oldest = consumedServerVerdictRequestHashes.first()
            consumedServerVerdictRequestHashes.remove(oldest)
        }

        consumedServerVerdictRequestHashes.add(requestHashEcho)
        return true
    }
}
