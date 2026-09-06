# Add project specific ProGuard rules here.
# Room components
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Firebase & Firestore
-keepattributes Signature, *Annotation*, EnclosingMethod
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Moshi & Retrofit
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Kotlin Coroutines
-keepattributes *Annotation*, InnerClasses, Signature
-keep class kotlinx.coroutines.** { *; }
