package com.lifeflow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.lifeflow.navigation.PublicShellNavHost
import com.lifeflow.ui.theme.LifeFlowTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        applyWindowSecurityHardening()

        val onboardingStore = OnboardingStateStore(this)

        showNativeStartupFrame()
        window.decorView.runAfterFirstDraw {
            installComposeContent(onboardingStore = onboardingStore)
        }
    }

    private fun showNativeStartupFrame() {
        setContentView(
            FrameLayout(this).apply {
                setBackgroundColor(Color.rgb(242, 243, 247))
            }
        )
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

private fun View.runAfterFirstDraw(action: () -> Unit) {
    var didPost = false
    val target = this
    val observer = viewTreeObserver

    val listener = object : ViewTreeObserver.OnDrawListener {
        override fun onDraw() {
            if (didPost) return

            didPost = true
            target.post {
                if (observer.isAlive) {
                    observer.removeOnDrawListener(this)
                }
                action()
            }
        }
    }

    observer.addOnDrawListener(listener)
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
        val entryPoint = withContext(Dispatchers.Default) {
            activity.requireStartupRuntimeEntryPoint()
        }

        startupRuntimeEntryPoint = entryPoint

        val startupReady = withContext(Dispatchers.Default) {
            entryPoint.ensureStarted()
        }

        startupBindings = if (startupReady) {
            activity.resolveStartedStartupBindings(entryPoint)
        } else {
            failedStartupBindings()
        }

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
        IntroSplashScreen()
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
