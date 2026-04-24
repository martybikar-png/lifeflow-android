package com.lifeflow.security

import android.content.Context

internal data class DeviceBindingSnapshot(
    val bindingId: String,
    val deviceFingerprint: String,
    val sessionKeyAlias: String,
    val authPerUseKeyAlias: String?,
    val attestationKeyAlias: String,
    val clientAuthKeyAlias: String,
    val registeredAtEpochMs: Long
)

internal class DeviceBindingStore(
    context: Context
) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun currentOrNull(): DeviceBindingSnapshot? {
        val bindingId = prefs.getString(KEY_BINDING_ID, null)
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val deviceFingerprint = prefs.getString(KEY_DEVICE_FINGERPRINT, null)
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val sessionKeyAlias = prefs.getString(KEY_SESSION_KEY_ALIAS, null)
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val attestationKeyAlias = prefs.getString(KEY_ATTESTATION_KEY_ALIAS, null)
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val clientAuthKeyAlias = prefs.getString(KEY_CLIENT_AUTH_KEY_ALIAS, null)
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val registeredAtEpochMs = prefs.getLong(KEY_REGISTERED_AT_EPOCH_MS, 0L)
            .takeIf { it > 0L }
            ?: return null

        return DeviceBindingSnapshot(
            bindingId = bindingId,
            deviceFingerprint = deviceFingerprint,
            sessionKeyAlias = sessionKeyAlias,
            authPerUseKeyAlias = prefs.getString(KEY_AUTH_PER_USE_KEY_ALIAS, null)
                ?.takeIf { it.isNotBlank() },
            attestationKeyAlias = attestationKeyAlias,
            clientAuthKeyAlias = clientAuthKeyAlias,
            registeredAtEpochMs = registeredAtEpochMs
        )
    }

    fun persist(
        snapshot: DeviceBindingSnapshot
    ) {
        val editor = prefs.edit()
            .putString(KEY_BINDING_ID, snapshot.bindingId)
            .putString(KEY_DEVICE_FINGERPRINT, snapshot.deviceFingerprint)
            .putString(KEY_SESSION_KEY_ALIAS, snapshot.sessionKeyAlias)
            .putString(KEY_ATTESTATION_KEY_ALIAS, snapshot.attestationKeyAlias)
            .putString(KEY_CLIENT_AUTH_KEY_ALIAS, snapshot.clientAuthKeyAlias)
            .putLong(KEY_REGISTERED_AT_EPOCH_MS, snapshot.registeredAtEpochMs)

        val authPerUseAlias = snapshot.authPerUseKeyAlias
        if (authPerUseAlias.isNullOrBlank()) {
            editor.remove(KEY_AUTH_PER_USE_KEY_ALIAS)
        } else {
            editor.putString(KEY_AUTH_PER_USE_KEY_ALIAS, authPerUseAlias)
        }

        val ok = editor.commit()
        if (!ok) {
            throw IllegalStateException("Device binding persist failed.")
        }
    }

    fun clear() {
        val ok = prefs.edit().clear().commit()
        if (!ok) {
            throw IllegalStateException("Device binding clear failed.")
        }
    }

    private companion object {
        private const val PREFS_NAME = "lifeflow_device_binding_v1"
        private const val KEY_BINDING_ID = "binding_id"
        private const val KEY_DEVICE_FINGERPRINT = "device_fingerprint"
        private const val KEY_SESSION_KEY_ALIAS = "session_key_alias"
        private const val KEY_AUTH_PER_USE_KEY_ALIAS = "auth_per_use_key_alias"
        private const val KEY_ATTESTATION_KEY_ALIAS = "attestation_key_alias"
        private const val KEY_CLIENT_AUTH_KEY_ALIAS = "client_auth_key_alias"
        private const val KEY_REGISTERED_AT_EPOCH_MS = "registered_at_epoch_ms"
    }
}
