package com.lifeflow.security.integrity

import android.content.Context
import android.util.Log
import com.lifeflow.BuildConfig
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

internal interface PlayIntegrityBootstrapHandle : AutoCloseable

private object NoOpPlayIntegrityBootstrapHandle : PlayIntegrityBootstrapHandle {
    override fun close() {
        PlayIntegrityVerifier.clearPreparedProvider()
    }
}

private class ActivePlayIntegrityBootstrapHandle : PlayIntegrityBootstrapHandle {
    override fun close() {
        PlayIntegrityVerifier.clearPreparedProvider()
    }
}

internal object PlayIntegrityBootstrapPreparation {

    private const val TAG = "PlayIntegrityBootstrap"

    fun start(
        applicationContext: Context,
        isInstrumentation: Boolean
    ): PlayIntegrityBootstrapHandle {
        if (isInstrumentation) {
            return NoOpPlayIntegrityBootstrapHandle
        }

        val cloudProjectNumber = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER
        if (cloudProjectNumber == 0L) {
            Log.i(
                TAG,
                "Play Integrity preparation skipped because PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER is not configured."
            )
            return NoOpPlayIntegrityBootstrapHandle
        }

        thread(
            start = true,
            isDaemon = true,
            name = "LifeFlow-PlayIntegrityPrepare"
        ) {
            when (
                val result = runBlocking {
                    PlayIntegrityVerifier.prepareTokenProvider(
                        context = applicationContext,
                        cloudProjectNumber = cloudProjectNumber
                    )
                }
            ) {
                PlayIntegrityVerifier.PreparationResult.Prepared -> {
                    Log.i(TAG, "Play Integrity token provider prepared successfully.")
                }

                is PlayIntegrityVerifier.PreparationResult.Failure -> {
                    Log.w(
                        TAG,
                        "Play Integrity preparation failed: ${result.error}"
                    )
                }

                PlayIntegrityVerifier.PreparationResult.NotConfigured -> {
                    Log.i(TAG, "Play Integrity preparation reported NotConfigured.")
                }
            }
        }

        return ActivePlayIntegrityBootstrapHandle()
    }
}
