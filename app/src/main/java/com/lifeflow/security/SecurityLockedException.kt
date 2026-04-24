package com.lifeflow.security

internal class SecurityLockedException(
    val lockedReason: String,
    cause: Throwable? = null
) : SecurityException(lockedReason, cause)
