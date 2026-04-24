package com.lifeflow.security

import android.content.Context
import java.util.UUID

internal class DeviceBindingManager(
    applicationContext: Context,
    private val store: DeviceBindingStore,
    private val sessionKeyAlias: String,
    private val authPerUseKeyAlias: String?,
    private val attestationKeyAlias: String,
    private val clientAuthKeyAlias: String
) {
    private val appContext = applicationContext.applicationContext

    fun ensureRegistered(): DeviceBindingSnapshot {
        val currentFingerprint = DeviceFingerprint.generate(appContext)
        val existing = store.currentOrNull()

        return when {
            existing == null -> createNewBinding(currentFingerprint)
            existing.deviceFingerprint != currentFingerprint ->
                throw SecurityException(
                    "Device binding mismatch. Stored binding belongs to a different device fingerprint."
                )

            existing.sessionKeyAlias != sessionKeyAlias ||
                existing.authPerUseKeyAlias != authPerUseKeyAlias ||
                existing.attestationKeyAlias != attestationKeyAlias ||
                existing.clientAuthKeyAlias != clientAuthKeyAlias ->
                refreshBinding(existing, currentFingerprint)

            else -> existing
        }
    }

    fun currentValidBindingOrNull(): DeviceBindingSnapshot? {
        val snapshot = store.currentOrNull() ?: return null
        val currentFingerprint = DeviceFingerprint.generate(appContext)

        return snapshot.takeIf { it.deviceFingerprint == currentFingerprint }
    }

    fun requireCurrentBinding(): DeviceBindingSnapshot {
        return currentValidBindingOrNull()
            ?: throw SecurityException(
                "Device binding is missing or no longer matches the current device."
            )
    }

    fun clear() {
        store.clear()
    }

    fun resetAndRebind(): DeviceBindingSnapshot {
        clear()
        return ensureRegistered()
    }

    private fun createNewBinding(
        currentFingerprint: String
    ): DeviceBindingSnapshot {
        val snapshot = DeviceBindingSnapshot(
            bindingId = UUID.randomUUID().toString(),
            deviceFingerprint = currentFingerprint,
            sessionKeyAlias = sessionKeyAlias,
            authPerUseKeyAlias = authPerUseKeyAlias,
            attestationKeyAlias = attestationKeyAlias,
            clientAuthKeyAlias = clientAuthKeyAlias,
            registeredAtEpochMs = System.currentTimeMillis()
        )
        store.persist(snapshot)
        return snapshot
    }

    private fun refreshBinding(
        existing: DeviceBindingSnapshot,
        currentFingerprint: String
    ): DeviceBindingSnapshot {
        val refreshed = existing.copy(
            deviceFingerprint = currentFingerprint,
            sessionKeyAlias = sessionKeyAlias,
            authPerUseKeyAlias = authPerUseKeyAlias,
            attestationKeyAlias = attestationKeyAlias,
            clientAuthKeyAlias = clientAuthKeyAlias,
            registeredAtEpochMs = System.currentTimeMillis()
        )
        store.persist(refreshed)
        return refreshed
    }
}
