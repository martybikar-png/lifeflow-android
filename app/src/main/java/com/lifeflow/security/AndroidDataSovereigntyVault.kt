package com.lifeflow.security

import android.content.Context
import com.lifeflow.domain.core.DataSovereigntyVault
import java.util.UUID

class AndroidDataSovereigntyVault(
    private val context: Context,
    private val keyManager: KeyManager
) : DataSovereigntyVault {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun isInitialized(): Boolean {
        val flag = prefs.getBoolean(KEY_VAULT_INITIALIZED, false)
        if (!flag) return false

        return try {
            keyManager.requireOperationalKeyPosture()
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun ensureInitialized() {
        if (isInitialized()) return

        keyManager.generateKey()
        keyManager.requireOperationalKeyPosture()

        val ok = prefs.edit()
            .putBoolean(KEY_VAULT_INITIALIZED, true)
            .commit()

        if (!ok) throw IllegalStateException("Vault initialization commit failed")
    }

    fun getIdentityVersion(id: UUID): Long {
        return prefs.getLong(versionKey(id), 0L)
    }

    @Synchronized
    fun nextIdentityVersion(id: UUID): Long {
        val current = getIdentityVersion(id)
        val next = current + 1L
        require(next > 0L) { "Vault version overflow for id=$id" }

        val ok = prefs.edit().putLong(versionKey(id), next).commit()
        if (!ok) throw IllegalStateException("Vault version commit failed for id=$id")

        return next
    }

    fun clearIdentityVersion(id: UUID) {
        val ok = prefs.edit().remove(versionKey(id)).commit()
        if (!ok) throw IllegalStateException("Vault version clear failed for id=$id")
    }

    @Synchronized
    fun resetVault() {
        keyManager.deleteKey()

        val ok = prefs.edit().clear().commit()
        if (!ok) throw IllegalStateException("Vault reset commit failed")
    }

    private fun versionKey(id: UUID): String = "identity_version_$id"

    private companion object {
        private const val PREFS_NAME = "lifeflow_vault"
        private const val KEY_VAULT_INITIALIZED = "vault_initialized"
    }
}
