package com.lifeflow.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal data class IntegrityTrustTlsMaterial(
    val sslSocketFactory: SSLSocketFactory,
    val hostnameVerifier: HostnameVerifier? = null
)

internal interface IntegrityTrustTlsMaterialProvider {
    fun materialFor(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig
    ): IntegrityTrustTlsMaterial
}

internal object UnconfiguredIntegrityTrustTlsMaterialProvider :
    IntegrityTrustTlsMaterialProvider {

    override fun materialFor(
        endpoint: IntegrityTrustTransportConfig.EndpointConfig
    ): IntegrityTrustTlsMaterial {
        throw SecurityException(
            "Integrity trust mTLS material is not configured for ${endpoint.host}:${endpoint.port}. " +
                "Secure verdict transport remains fail-closed."
        )
    }
}

internal object IntegrityTrustTlsMaterialBootstrap {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val EC_CURVE = "secp256r1"
    private const val CLIENT_AUTH_CHALLENGE_CONTEXT =
        "lifeflow-integrity-client-auth-v1"

    fun create(
        applicationContext: Context
    ): IntegrityTrustTlsMaterialProvider {
        ensureClientAuthKeyMaterial(
            applicationContext = applicationContext.applicationContext
        )

        return AndroidKeystoreIntegrityTrustTlsMaterialProvider(
            clientAuthKeyAlias = INTEGRITY_CLIENT_AUTH_KEY_ALIAS
        )
    }

    private fun ensureClientAuthKeyMaterial(
        applicationContext: Context
    ) {
        if (hasUsableClientAuthEntry()) {
            return
        }

        deleteClientAuthKeySilently()
        generateClientAuthKeyPair(applicationContext)
    }

    private fun hasUsableClientAuthEntry(): Boolean {
        val keyStore = loadAndroidKeyStore()
        if (!keyStore.containsAlias(INTEGRITY_CLIENT_AUTH_KEY_ALIAS)) {
            return false
        }

        val entry = keyStore.getEntry(INTEGRITY_CLIENT_AUTH_KEY_ALIAS, null)
        val privateKeyEntry = entry as? KeyStore.PrivateKeyEntry ?: return false
        return privateKeyEntry.certificateChain?.isNotEmpty() == true
    }

    private fun generateClientAuthKeyPair(
        applicationContext: Context
    ) {
        val challenge = buildClientAuthChallenge(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                generateClientAuthKeyPairInternal(
                    challenge = challenge,
                    useStrongBox = true
                )
                return
            } catch (_: StrongBoxUnavailableException) {
            }
        }

        generateClientAuthKeyPairInternal(
            challenge = challenge,
            useStrongBox = false
        )
    }

    private fun generateClientAuthKeyPairInternal(
        challenge: ByteArray,
        useStrongBox: Boolean
    ) {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            INTEGRITY_CLIENT_AUTH_KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
            .setDigests(
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512
            )
            .setAttestationChallenge(challenge)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && useStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        keyPairGenerator.initialize(builder.build())
        keyPairGenerator.generateKeyPair()
    }

    private fun buildClientAuthChallenge(
        applicationContext: Context
    ): ByteArray {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(CLIENT_AUTH_CHALLENGE_CONTEXT.toByteArray(Charsets.UTF_8))
        digest.update('\n'.code.toByte())
        digest.update(applicationContext.packageName.toByteArray(Charsets.UTF_8))
        digest.update('\n'.code.toByte())
        digest.update(randomBytes)
        return digest.digest()
    }

    private fun deleteClientAuthKeySilently() {
        runCatching {
            val keyStore = loadAndroidKeyStore()
            if (keyStore.containsAlias(INTEGRITY_CLIENT_AUTH_KEY_ALIAS)) {
                keyStore.deleteEntry(INTEGRITY_CLIENT_AUTH_KEY_ALIAS)
            }
        }
    }

    private fun loadAndroidKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }
}

internal class AndroidKeystoreIntegrityTrustTlsMaterialProvider(
    private val clientAuthKeyAlias: String
) : IntegrityTrustTlsMaterialProvider {

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

        val keyManagerPassword = temporaryPassword()
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
            PinnedSpkiTrustManager(
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

    private fun temporaryPassword(): CharArray {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP).toCharArray()
    }

    private class PinnedSpkiTrustManager(
        private val delegate: X509TrustManager,
        private val pinnedSpkiSha256: Set<String>
    ) : X509TrustManager {

        override fun getAcceptedIssuers(): Array<X509Certificate> =
            delegate.acceptedIssuers

        override fun checkClientTrusted(
            chain: Array<X509Certificate>,
            authType: String
        ) {
            delegate.checkClientTrusted(chain, authType)
        }

        override fun checkServerTrusted(
            chain: Array<X509Certificate>,
            authType: String
        ) {
            delegate.checkServerTrusted(chain, authType)

            if (chain.isEmpty()) {
                throw CertificateException(
                    "Integrity trust server certificate chain is empty."
                )
            }

            val observedPins = chain.mapTo(linkedSetOf()) { certificate ->
                certificate.toPinnedSpkiSha256()
            }

            if (observedPins.none { it in pinnedSpkiSha256 }) {
                throw CertificateException(
                    "Integrity trust server certificate pin mismatch."
                )
            }
        }

        private fun X509Certificate.toPinnedSpkiSha256(): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(publicKey.encoded)

            return "sha256/" + Base64.encodeToString(digest, Base64.NO_WRAP)
        }
    }

    private companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }
}
