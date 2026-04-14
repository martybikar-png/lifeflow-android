package com.lifeflow.security

internal class SecurityCryptoBindings(
    val sessionKeyManager: KeyManager,
    val sessionEncryptionService: EncryptionService,
    val authPerUseKeyManager: KeyManager?,
    val authPerUseEncryptionService: EncryptionService?
)
