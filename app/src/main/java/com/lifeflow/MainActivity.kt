package com.lifeflow

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.lifeflow.navigation.PublicShellNavHost
import com.lifeflow.ui.theme.LifeFlowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val StartupHandoffMinimumVisibleMs = 4_000L

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        applyWindowSecurityHardening()

        val onboardingStore = OnboardingStateStore(this)

        installComposeContent(onboardingStore = onboardingStore)
    }

    private fun installComposeContent(onboardingStore: OnboardingStateStore) {
        setContent {
            LifeFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainActivityAppContent(
                        activity = this@MainActivity,
                        onboardingStore = onboardingStore,
                        appPackageName = packageName,
                        onStartIntent = { intent -> startActivity(intent) },
                        onRecreateActivity = { recreate() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainActivityAppContent(
    activity: FragmentActivity,
    onboardingStore: OnboardingStateStore,
    appPackageName: String,
    onStartIntent: (Intent) -> Unit,
    onRecreateActivity: () -> Unit
) {

    var startupRuntimeEntryPoint by remember {
        mutableStateOf<StartupRuntimeEntryPoint?>(null)
    }
    var startupBindings by remember {
        mutableStateOf<StartupBindings?>(null)
    }
    var onboardingCompleted by remember(onboardingStore) {
        mutableStateOf(onboardingStore.isCompleted())
    }

    LaunchedEffect(Unit) {
        val handoffStartedAtMs = System.nanoTime() / 1_000_000L

        val entryPoint = withContext(Dispatchers.Default) {
            activity.requireStartupRuntimeEntryPoint()
        }

        val resolvedBindings = withContext(Dispatchers.Default) {
            val startupReady = entryPoint.ensureStarted()
            if (startupReady) {
                activity.resolveStartedStartupBindings(entryPoint)
            } else {
                failedStartupBindings()
            }
        }

        val elapsedMs = (System.nanoTime() / 1_000_000L) - handoffStartedAtMs
        val remainingMs = StartupHandoffMinimumVisibleMs - elapsedMs
        if (remainingMs > 0L) {
            delay(remainingMs)
        }

        startupBindings = resolvedBindings
        startupRuntimeEntryPoint = entryPoint

        withFrameNanos { }
        activity.reportFullyDrawn()
    }

    if (!onboardingCompleted) {
        PublicShellNavHost(
            startAtHome = false,
            onOnboardingCompleted = {
                onboardingStore.setCompleted()
                onboardingCompleted = true
            },
            completeOnboardingLocally = false
        )
        return
    }

    val entryPoint = startupRuntimeEntryPoint ?: run {
        StartupHandoffScreen()
        return
    }

    AppEntry(
        startupRuntimeEntryPoint = entryPoint,
        startupBindings = startupBindings,
        initialOnboardingCompleted = onboardingCompleted,
        onMarkOnboardingCompleted = {
            onboardingStore.setCompleted()
            onboardingCompleted = true
        },
        appPackageName = appPackageName,
        onStartIntent = onStartIntent,
        onRecreateActivity = onRecreateActivity
    )
}

@Composable
private fun StartupHandoffScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Preparing LifeFlow.",
                color = Color(0xFF111111),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Secure startup...",
                color = Color(0xFF4A4F57),
                fontSize = 15.sp
            )
            Text(
                text = "Syncing data...",
                color = Color(0xFF4A4F57),
                fontSize = 15.sp
            )
        }
    }
}
