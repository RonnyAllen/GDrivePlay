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

# Retrofit 2
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.** <methods>;
}
-keep class com.driveplay.data.remote.models.** { *; }

# Gson
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Google Cast SDK
-keep class com.google.android.gms.cast.** { *; }
-dontwarn com.google.android.gms.cast.**

# NanoHTTPD (Cast proxy server)
-keep class fi.iki.elonen.** { *; }

# Google API client Apache HTTP transport warnings
-dontwarn org.apache.http.**
