package com.lifeflow.security.hardening

import android.content.Context

private val suspiciousInstrumentationPackages = setOf(
    "de.robv.android.xposed.installer",
    "org.lsposed.manager",
    "org.meowcat.edxposed.manager",
    "com.saurik.substrate"
)

private val suspiciousInstrumentationClasses = setOf(
    "de.robv.android.xposed.XposedBridge",
    "de.robv.android.xposed.XposedHelpers",
    "com.saurik.substrate.MS"
)

internal fun detectHardeningInstrumentation(
    context: Context,
    findings: MutableList<String>
): Boolean {
    val installedPackages = suspiciousInstrumentationPackages.filterTo(mutableSetOf()) { pkg ->
        isHardeningPackageInstalled(context, pkg)
    }

    val resolvableClasses = suspiciousInstrumentationClasses.filterTo(mutableSetOf()) { className ->
        canResolveHardeningClass(className)
    }

    return detectHardeningInstrumentationFromSignals(
        installedPackages = installedPackages,
        resolvableClasses = resolvableClasses,
        findings = findings
    )
}

internal fun detectHardeningInstrumentationFromSignals(
    installedPackages: Set<String>,
    resolvableClasses: Set<String>,
    findings: MutableList<String>
): Boolean {
    installedPackages.forEach { pkg ->
        findings += "Instrumentation: suspicious package detected ($pkg)"
    }

    resolvableClasses.forEach { className ->
        findings += "Instrumentation: suspicious class resolved ($className)"
    }

    return installedPackages.isNotEmpty() || resolvableClasses.isNotEmpty()
}

private fun canResolveHardeningClass(className: String): Boolean {
    return try {
        Class.forName(className)
        true
    } catch (_: Throwable) {
        false
    }
}
