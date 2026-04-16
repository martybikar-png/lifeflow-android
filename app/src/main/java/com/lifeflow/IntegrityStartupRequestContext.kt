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
    val requestedAtEpochMs: Long
) {
    init {
        require(schemaVersion > 0) { "schemaVersion must be > 0." }
        require(packageName.isNotBlank()) { "packageName must not be blank." }
        require(versionName.isNotBlank()) { "versionName must not be blank." }
        require(versionCode > 0) { "versionCode must be > 0." }
        require(buildType.isNotBlank()) { "buildType must not be blank." }
        require(startupPhase.isNotBlank()) { "startupPhase must not be blank." }
        require(requestedAtEpochMs > 0L) { "requestedAtEpochMs must be > 0." }
    }

    fun serializeIntegrityPayload(): String {
        return buildString {
            appendLine("schemaVersion=$schemaVersion")
            appendLine("packageName=$packageName")
            appendLine("versionName=$versionName")
            appendLine("versionCode=$versionCode")
            appendLine("buildType=$buildType")
            appendLine("startupPhase=$startupPhase")
            append("requestedAtEpochMs=$requestedAtEpochMs")
        }
    }
}
