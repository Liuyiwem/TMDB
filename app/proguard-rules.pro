# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Since we keep the line number information, hide the original source file name.
-renamesourcefileattribute SourceFile

# navigation3 的 NavKeySerializer 用 value::class.java.name 把類別名寫進 saved state，
# 還原時再 Class.forName 讀回來。混淆後的名字會被存進去——同一次 build 內自洽，
# 但 app 更新後（新的一次 R8、可能是不同的 mapping）還原舊 bundle 會 ClassNotFoundException。
-keepnames class * implements androidx.navigation3.runtime.NavKey