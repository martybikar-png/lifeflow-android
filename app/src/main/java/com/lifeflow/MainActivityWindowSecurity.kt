package com.lifeflow

import android.os.Build
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity

/**
 * Applies activity window hardening for production builds.
 *
 * Purpose:
 * - keep MainActivity thin
 * - centralize window-level UI hardening in one place
 */
internal fun FragmentActivity.applyWindowSecurityHardening() {
    if (BuildConfig.DEBUG) return

    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        runCatching {
            javaClass
                .getMethod("setHideOverlayWindows", Boolean::class.javaPrimitiveType)
                .invoke(this, true)
        }
    }
}
