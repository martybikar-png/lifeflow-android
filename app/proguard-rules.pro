# LifeFlow release shrink / obfuscation baseline
#
# Principle:
# - keep only code that is used indirectly at runtime
# - let R8 shrink and obfuscate the security packages unless a proven runtime dependency requires preservation
# - let DexGuard later harden the sensitive packages on top of this minimal baseline
#
# Android components and standard framework entry points are already covered by:
# getDefaultProguardFile("proguard-android-optimize.txt")

# Preserve metadata commonly needed by Kotlin / annotations / generic signatures.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Hide original source file names in release outputs.
-renamesourcefileattribute SourceFile

# Intentionally no broad package-wide -keep rules here.
# Security, integrity, keystore, attestation, and authority code should stay
# shrinkable and obfuscatable unless a concrete indirect runtime path proves otherwise.
