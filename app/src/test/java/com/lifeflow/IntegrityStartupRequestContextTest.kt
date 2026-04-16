package com.lifeflow

import org.junit.Assert.assertEquals
import org.junit.Test

class IntegrityStartupRequestContextTest {

    @Test
    fun `serializeIntegrityPayload is canonical and deterministic`() {
        val context = IntegrityStartupRequestContext(
            schemaVersion = 1,
            packageName = "com.lifeflow",
            versionName = "1.0",
            versionCode = 7,
            buildType = "release",
            startupPhase = "application_startup",
            requestedAtEpochMs = 123456789L
        )

        val payload = context.serializeIntegrityPayload()

        assertEquals(
            """
schemaVersion=1
packageName=com.lifeflow
versionName=1.0
versionCode=7
buildType=release
startupPhase=application_startup
requestedAtEpochMs=123456789
""".trimIndent(),
            payload
        )
    }
}
