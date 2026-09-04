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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson serializes these models using their field names. Preserve the names to
# keep existing SharedPreferences favorite/recent data compatible with R8.
-keepclassmembers class com.dearyoti.doahindu.model.** {
    <fields>;
}

# Gson reads the generic type from TypeToken at runtime. Keep it in R8 builds
# so existing recent-history data can be deserialized safely.
-keepattributes Signature
-keep,allowobfuscation class com.google.gson.reflect.TypeToken
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
