package com.lifeflow

internal enum class ProtectedRuntimeRebuildSource(
    val payloadValue: String
) {
    STARTUP_INTEGRITY_POLICY("startup_integrity_policy"),
    STARTUP_INTEGRITY_REQUEST_FAILURE("startup_integrity_request_failure"),
    RUNTIME_SECURITY_SIGNAL("runtime_security_signal"),
    EXPLICIT_SECURITY_RECOVERY("explicit_security_recovery"),
    INTERNAL("internal")
}
