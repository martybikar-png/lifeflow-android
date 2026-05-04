package com.lifeflow

import android.content.Intent
import android.graphics.Color
import android.widget.ImageView
import android.view.Gravity
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
        window.decorView.runAfterFirstWindowFocus {
            installComposeContent(onboardingStore = onboardingStore)
        }
    }

    private fun showNativeStartupFrame() {
        val iconSizePx = (132 * resources.displayMetrics.density).toInt()

        setContentView(
            FrameLayout(this).apply {
                setBackgroundColor(Color.rgb(34, 205, 247))
                addView(
                    ImageView(this@MainActivity).apply {
                        setImageResource(R.drawable.lifeflow_splash_icon)
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    },
                    FrameLayout.LayoutParams(
                        iconSizePx,
                        iconSizePx,
                        Gravity.CENTER
                    )
                )
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

private fun View.runAfterFirstWindowFocus(action: () -> Unit) {
    var didPost = false
    val target = this
    val observer = viewTreeObserver

    val listener = object : ViewTreeObserver.OnWindowFocusChangeListener {
        override fun onWindowFocusChanged(hasFocus: Boolean) {
            if (!hasFocus || didPost) return

            didPost = true
            target.post {
                if (observer.isAlive) {
                    observer.removeOnWindowFocusChangeListener(this)
                }
                action()
            }
        }
    }

    observer.addOnWindowFocusChangeListener(listener)

    if (hasWindowFocus()) {
        listener.onWindowFocusChanged(true)
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
