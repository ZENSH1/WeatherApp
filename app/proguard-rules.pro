# Libraries ship their own R8 rules; only app-specific ones go here.

# Keep line numbers in crash reports, but hide the original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp optionally uses these TLS providers when they are on the classpath.
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
