# R8 rules for the release build (isMinifyEnabled = true).
# Retrofit, OkHttp, kotlinx.serialization, Room, Hilt and Coil ship their own consumer rules,
# so only app-specific rules belong here.

# Keep line numbers in crash reports, but hide the original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp optionally uses these TLS providers when they are on the classpath.
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
