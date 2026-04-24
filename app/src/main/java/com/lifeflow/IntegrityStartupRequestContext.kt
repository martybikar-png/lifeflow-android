package com.lifeflow

/**
 * Structured startup request context for integrity binding.
 *
 * Purpose:
 * - keep startup payload semantically explicit
 * - keep serialization canonical and deterministic
 * - make future backend claim validation easier
 */
internal data class IntegrityStartupRequestContext(
    val schemaVersion: Int,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val buildType: String,
    val startupPhase: String,
    val startupTrigger: String,
    val startupProcessSequence: Int,
    val startupRequestId: String,
    val requestedAtEpochMs: Long,
    val startupRestartReason: String? = null,
    val startupAutoRecoveryAttempt: Int? = null,
    val startupRebuildSource: String? = null,
    val startupRecoverySignal: String? = null,
    val attestationStatus: String? = null,
    val attestationKeyAlias: String? = null,
    val attestationChainEntryCount: Int? = null,
    val attestationChallengeSha256: String? = null,
    val attestationLeafCertificateSha256: String? = null,
    val attestationStrongBoxRequested: Boolean? = null,
    val attestationFailureReason: String? = null
) {
    init {
        require(schemaVersion > 0) { "schemaVersion must be > 0." }
        require(packageName.isNotBlank()) { "packageName must not be blank." }
        require(versionName.isNotBlank()) { "versionName must not be blank." }
        require(versionCode > 0) { "versionCode must be > 0." }
        require(buildType.isNotBlank()) { "buildType must not be blank." }
        require(startupPhase.isNotBlank()) { "startupPhase must not be blank." }
        require(startupTrigger.isNotBlank()) { "startupTrigger must not be blank." }
        require(startupProcessSequence > 0) { "startupProcessSequence must be > 0." }
        require(startupRequestId.isNotBlank()) { "startupRequestId must not be blank." }
        require(requestedAtEpochMs > 0L) { "requestedAtEpochMs must be > 0." }

        startupRestartReason?.let {
            require(it.isNotBlank()) { "startupRestartReason must not be blank when present." }
        }
        startupAutoRecoveryAttempt?.let {
            require(it > 0) { "startupAutoRecoveryAttempt must be > 0 when present." }
        }
        startupRebuildSource?.let {
            require(it.isNotBlank()) { "startupRebuildSource must not be blank when present." }
        }
        startupRecoverySignal?.let {
            require(it.isNotBlank()) { "startupRecoverySignal must not be blank when present." }
        }

        attestationStatus?.let {
            require(it.isNotBlank()) { "attestationStatus must not be blank when present." }
        }
        attestationKeyAlias?.let {
            require(it.isNotBlank()) { "attestationKeyAlias must not be blank when present." }
        }
        attestationChainEntryCount?.let {
            require(it >= 0) { "attestationChainEntryCount must be >= 0 when present." }
        }
        attestationChallengeSha256?.let {
            require(it.isNotBlank()) { "attestationChallengeSha256 must not be blank when present." }
        }
        attestationLeafCertificateSha256?.let {
            require(it.isNotBlank()) { "attestationLeafCertificateSha256 must not be blank when present." }
        }
        attestationFailureReason?.let {
            require(it.isNotBlank()) { "attestationFailureReason must not be blank when present." }
        }
    }

    fun serializeIntegrityPayload(): String {
        return buildString {
            appendLine("schemaVersion=$schemaVersion")
            appendLine("packageName=$packageName")
            appendLine("versionName=$versionName")
            appendLine("versionCode=$versionCode")
            appendLine("buildType=$buildType")
            appendLine("startupPhase=$startupPhase")
            appendLine("startupTrigger=$startupTrigger")
            appendLine("startupProcessSequence=$startupProcessSequence")
            appendLine("startupRequestId=$startupRequestId")
            appendLine("requestedAtEpochMs=$requestedAtEpochMs")
            startupRestartReason?.let { appendLine("startupRestartReason=$it") }
            startupAutoRecoveryAttempt?.let { appendLine("startupAutoRecoveryAttempt=$it") }
            startupRebuildSource?.let { appendLine("startupRebuildSource=$it") }
            startupRecoverySignal?.let { appendLine("startupRecoverySignal=$it") }
            attestationStatus?.let { appendLine("attestationStatus=$it") }
            attestationKeyAlias?.let { appendLine("attestationKeyAlias=$it") }
            attestationChainEntryCount?.let { appendLine("attestationChainEntryCount=$it") }
            attestationChallengeSha256?.let { appendLine("attestationChallengeSha256=$it") }
            attestationLeafCertificateSha256?.let { appendLine("attestationLeafCertificateSha256=$it") }
            attestationStrongBoxRequested?.let { appendLine("attestationStrongBoxRequested=$it") }
            attestationFailureReason?.let { append("attestationFailureReason=$it") } ?: run {
                if (endsWith("\n")) {
                    deleteCharAt(length - 1)
                }
            }
        }
    }
}
