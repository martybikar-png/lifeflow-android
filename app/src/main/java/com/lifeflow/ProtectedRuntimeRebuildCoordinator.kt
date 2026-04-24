package com.lifeflow

import java.util.concurrent.atomic.AtomicBoolean

internal data class ProtectedRuntimeRebuildAdmission(
    val allowed: Boolean,
    val skipMessage: String? = null
)

internal class ProtectedRuntimeRebuildCoordinator {

    private val rebuildInFlight = AtomicBoolean(false)

    fun tryAcquire(
        request: ProtectedRuntimeRebuildRequest
    ): ProtectedRuntimeRebuildAdmission {
        return if (rebuildInFlight.compareAndSet(false, true)) {
            ProtectedRuntimeRebuildAdmission(
                allowed = true
            )
        } else {
            ProtectedRuntimeRebuildAdmission(
                allowed = false,
                skipMessage =
                    "Protected runtime rebuild skipped: rebuild already in progress " +
                        "(${request.reason.name})."
            )
        }
    }

    fun release() {
        rebuildInFlight.set(false)
    }
}
