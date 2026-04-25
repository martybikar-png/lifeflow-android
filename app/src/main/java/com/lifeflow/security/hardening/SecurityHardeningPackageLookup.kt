package com.lifeflow.security.hardening

import android.content.Context

internal fun isHardeningPackageInstalled(
    context: Context,
    packageName: String
): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: Throwable) {
        false
    }
}
