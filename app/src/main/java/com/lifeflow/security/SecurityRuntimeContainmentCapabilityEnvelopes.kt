package com.lifeflow.security

internal fun securityRuntimeNormalEnvelope(): SecurityRuntimeCapabilityEnvelope =
    SecurityRuntimeCapabilityEnvelope(
        allowProtectedRuntime = true,
        allowSensitiveOperations = true,
        allowExternalAuthority = true,
        allowSecretUnlock = true,
        allowStateMutation = true,
        allowRecoveryFlow = true
    )

internal fun securityRuntimeGuardedEnvelope(): SecurityRuntimeCapabilityEnvelope =
    SecurityRuntimeCapabilityEnvelope(
        allowProtectedRuntime = true,
        allowSensitiveOperations = true,
        allowExternalAuthority = true,
        allowSecretUnlock = true,
        allowStateMutation = true,
        allowRecoveryFlow = true
    )

internal fun securityRuntimeRestrictedEnvelope(): SecurityRuntimeCapabilityEnvelope =
    SecurityRuntimeCapabilityEnvelope(
        allowProtectedRuntime = true,
        allowSensitiveOperations = false,
        allowExternalAuthority = false,
        allowSecretUnlock = false,
        allowStateMutation = true,
        allowRecoveryFlow = true
    )

internal fun securityRuntimeRecoveryOnlyEnvelope(): SecurityRuntimeCapabilityEnvelope =
    SecurityRuntimeCapabilityEnvelope(
        allowProtectedRuntime = false,
        allowSensitiveOperations = false,
        allowExternalAuthority = false,
        allowSecretUnlock = false,
        allowStateMutation = false,
        allowRecoveryFlow = true
    )
