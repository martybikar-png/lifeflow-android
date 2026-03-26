package com.lifeflow

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.lifeflow.ui.theme.LifeFlowTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyWindowSecurityHardening()

        val app = application as LifeFlowApplication
        val startupBindings = resolveStartupBindings(app)
        setContent {
            LifeFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!startupBindings.startupReady) {
                        StartupFailureContent(
                            app = app,
                            appPackageName = packageName,
                            onStartIntent = { intent -> startActivity(intent) },
                            onRecreateActivity = { recreate() }
                        )
                    } else {
                        MainActivityContent(
                            viewModel = requireNotNull(startupBindings.viewModel),
                            biometricAuthManager = requireNotNull(startupBindings.biometricAuthManager),
                            appPackageName = packageName,
                            onStartIntent = { intent -> startActivity(intent) }
                        )
                    }
                }
            }
        }
    }

    private fun applyWindowSecurityHardening() {
        if (BuildConfig.DEBUG) return

        // Prevent screenshots, screen recording, and app switcher previews on sensitive surfaces.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Reduce overlay/tapjacking risk on supported Android versions without requiring
        // the compile-time symbol to be present.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                javaClass
                    .getMethod("setHideOverlayWindows", Boolean::class.javaPrimitiveType)
                    .invoke(this, true)
            }
        }
    }
}
