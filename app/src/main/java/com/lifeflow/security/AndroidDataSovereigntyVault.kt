package com.lifeflow.security

import android.content.Context
import com.lifeflow.domain.core.DataSovereigntyVault

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

        prefs.edit()
            .putBoolean(KEY_VAULT_INITIALIZED, true)
            .apply()
    }

    private companion object {
        private const val PREFS_NAME = "lifeflow_vault"
        private const val KEY_VAULT_INITIALIZED = "vault_initialized"
    }
}