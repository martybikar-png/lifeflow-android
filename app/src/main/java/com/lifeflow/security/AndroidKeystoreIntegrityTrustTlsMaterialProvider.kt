package com.lifeflow.security

import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class AndroidKeystoreIntegrityTrustTlsMaterialProvider(
    private val clientAuthKeyAlias: String
) : IntegrityTrustTlsMaterialProvider {
    private val secureRandom = SecureRandom()

    override fun materialFor(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig
    ): IntegrityTrustTlsMaterial {
        val sslContext = SSLContext.getInstance("TLS")
        val keyManagers = createClientAuthKeyManagers()
        val trustManager = createTransportTrustManager(
            securityPolicy = endpoint.securityPolicy
        )

        sslContext.init(
            keyManagers,
            arrayOf<TrustManager>(trustManager),
            null
        )

        return IntegrityTrustTlsMaterial(
            sslSocketFactory = sslContext.socketFactory
        )
    }

    private fun createClientAuthKeyManagers() = run {
        val androidKeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        androidKeyStore.load(null)

        val entry = androidKeyStore.getEntry(clientAuthKeyAlias, null)
            as? KeyStore.PrivateKeyEntry
            ?: throw SecurityException(
                "Integrity trust client-auth key material is missing for alias=$clientAuthKeyAlias."
            )

        val keyManagerPassword = newInMemoryKeyStorePassword()
        try {
            val inMemoryKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            inMemoryKeyStore.load(null, keyManagerPassword)
            inMemoryKeyStore.setKeyEntry(
                clientAuthKeyAlias,
                entry.privateKey,
                keyManagerPassword,
                entry.certificateChain
            )

            val keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            )
            keyManagerFactory.init(inMemoryKeyStore, keyManagerPassword)
            keyManagerFactory.keyManagers
        } finally {
            keyManagerPassword.fill('\u0000')
        }
    }

    private fun createTransportTrustManager(
        securityPolicy: IntegrityTrustTransportConfig.SecurityPolicy
    ): X509TrustManager {
        val delegate = platformTrustManager()

        return if (securityPolicy.enforceCertificatePins) {
            IntegrityTrustPinnedSpkiTrustManager(
                delegate = delegate,
                pinnedSpkiSha256 = securityPolicy.pinnedSpkiSha256
            )
        } else {
            delegate
        }
    }

    private fun platformTrustManager(): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)

        return trustManagerFactory.trustManagers
            .filterIsInstance<X509TrustManager>()
            .firstOrNull()
            ?: throw SecurityException(
                "Platform X509TrustManager is unavailable for integrity trust transport."
            )
    }

    private fun newInMemoryKeyStorePassword(): CharArray {
        val alphabet = IN_MEMORY_KEYSTORE_PASSWORD_ALPHABET
        return CharArray(IN_MEMORY_KEYSTORE_PASSWORD_LENGTH) {
            alphabet[secureRandom.nextInt(alphabet.size)]
        }
    }

    private companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val IN_MEMORY_KEYSTORE_PASSWORD_LENGTH = 32
        private val IN_MEMORY_KEYSTORE_PASSWORD_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
    }
}