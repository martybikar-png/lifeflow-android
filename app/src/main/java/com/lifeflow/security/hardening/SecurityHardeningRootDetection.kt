package com.lifeflow.security.hardening

import android.content.Context
import android.os.Build
import java.io.File

private val hardeningRootSuPaths = setOf(
    "/system/bin/su",
    "/system/xbin/su",
    "/sbin/su",
    "/data/local/xbin/su",
    "/data/local/bin/su",
    "/data/local/su"
)

private val hardeningRootPackages = setOf(
    "com.topjohnwu.magisk",
    "com.noshufou.android.su",
    "eu.chainfire.supersu",
    "com.koushikdutta.superuser"
)

internal fun detectHardeningRoot(
    context: Context,
    findings: MutableList<String>
): Boolean {
    var detected = false

    hardeningRootSuPaths.forEach { path ->
        if (File(path).exists()) {
            findings += "Root: su binary found at $path"
            detected = true
        }
    }

    hardeningRootPackages.forEach { pkg ->
        if (isHardeningPackageInstalled(context, pkg)) {
            findings += "Root: root app detected ($pkg)"
            detected = true
        }
    }

    val buildTags = Build.TAGS
    if (buildTags != null && buildTags.contains("test-keys")) {
        findings += "Root: test-keys build tag detected"
        detected = true
    }

    return detected
}
