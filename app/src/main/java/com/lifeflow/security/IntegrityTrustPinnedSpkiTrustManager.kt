package com.lifeflow.security

import android.util.Base64
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

internal class IntegrityTrustPinnedSpkiTrustManager(
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