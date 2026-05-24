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

# ── Kotlin Coroutines & R8 Full Mode Fixes ──────────────────────────────────
# Prevents java.lang.Class cannot be cast to java.lang.reflect.ParameterizedType
-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault, *Annotation*, KotlinMetadata

# Keep Kotlin Metadata (required for reflection and generic matching)
-keep class kotlin.Metadata { *; }

# Keep Continuation interfaces and classes intact for suspend functions
-keep class kotlin.coroutines.Continuation { *; }
-keep interface kotlin.coroutines.Continuation { *; }
-keep class kotlin.coroutines.SafeContinuation { *; }
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep GDrivePlay API interfaces and data models fully intact
-keep interface com.driveplay.data.remote.DriveApiService { *; }
-keepclassmembers interface com.driveplay.data.remote.DriveApiService {
    <methods>;
}
-keep class com.driveplay.data.remote.models.** { *; }
-keep class com.driveplay.domain.model.** { *; }

# Keep internal coroutine state variables
-keepclassmembers class * {
    *** label;
}
