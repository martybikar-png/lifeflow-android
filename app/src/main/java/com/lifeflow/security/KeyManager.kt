package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyManager(
    private val alias: String = "LifeFlow_Master_Key"
) {

    private val androidKeyStore = "AndroidKeyStore"

    @Synchronized
    fun generateKey() {
        if (keyExists()) return

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            androidKeyStore
        )

        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        // Biometric STRONG only (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                30, // valid for 30 seconds after auth
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    @Synchronized
    fun getKey(): SecretKey {
        val keyStore = loadKeyStore()
        val key = keyStore.getKey(alias, null)
            ?: throw IllegalStateException("Master key not found")

        if (key !is SecretKey) {
            throw IllegalStateException("Invalid key type")
        }

        return key
    }

    @Synchronized
    fun deleteKey() {
        val keyStore = loadKeyStore()
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    fun keyExists(): Boolean {
        val keyStore = loadKeyStore()
        return keyStore.containsAlias(alias)
    }

    private fun loadKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        return keyStore
    }
}