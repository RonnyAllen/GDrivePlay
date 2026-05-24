# Google Drive API — preserve all fields used in JSON deserialization
-keep class com.google.api.services.drive.model.** { *; }
-keep class com.google.api.client.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.internal.publicsuffix.** { *; }

# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Google Cast SDK
-keep class com.google.android.gms.cast.** { *; }
-dontwarn com.google.android.gms.cast.**

# NanoHTTPD (Cast proxy server)
-keep class fi.iki.elonen.** { *; }
