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
        return flag && keyManager.keyExists()
    }

    override fun ensureInitialized() {
        if (isInitialized()) return

        keyManager.generateKey()

        // commit(): deterministic persistence
        val ok = prefs.edit()
            .putBoolean(KEY_VAULT_INITIALIZED, true)
            .commit()

        if (!ok) throw IllegalStateException("Vault initialization commit failed")
    }

    // ------------------------------------------------------------
    // Phase D — Monotonic Identity Versioning (rollback mitigation)
    // ------------------------------------------------------------

    /**
     * Returns current monotonic version for the identity.
     * 0 means "unknown / legacy".
     */
    fun getIdentityVersion(id: UUID): Long {
        return prefs.getLong(versionKey(id), 0L)
    }

    /**
     * Increments and persists identity version (monotonic).
     * Returns the new version (>= 1).
     */
    @Synchronized
    fun nextIdentityVersion(id: UUID): Long {
        val current = getIdentityVersion(id)
        val next = current + 1L

        // commit(): ensures monotonic step is not lost in async apply()
        val ok = prefs.edit().putLong(versionKey(id), next).commit()
        if (!ok) throw IllegalStateException("Vault version commit failed for id=$id")

        return next
    }

    /**
     * Clears identity version tracking (e.g., delete).
     */
    fun clearIdentityVersion(id: UUID) {
        prefs.edit().remove(versionKey(id)).commit()
    }

    /**
     * Phase F (Variant 1) — Vault reset:
     * - deletes keystore master key
     * - clears vault prefs (including initialized + all identity versions)
     * Deterministic commit, fail if persistence fails.
     */
    @Synchronized
    fun resetVault() {
        // 1) Delete key material first (hard stop)
        keyManager.deleteKey()

        // 2) Clear all vault state deterministically
        val ok = prefs.edit().clear().commit()
        if (!ok) throw IllegalStateException("Vault reset commit failed")
    }

    private fun versionKey(id: UUID): String = "identity_version_$id"

    private companion object {
        private const val PREFS_NAME = "lifeflow_vault"
        private const val KEY_VAULT_INITIALIZED = "vault_initialized"
    }
}