# Proguard & R8 Rules for DyslexiaReader

# Keep ML Kit components
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep Tesseract 4 Android JNI & Native bindings
-keep class cz.adaptech.tesseract4android.** { *; }
-keepclassmembers class * {
    native <methods>;
}

# Keep Room generated classes and entities
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.TypeConverter { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Firebase
-dontwarn com.google.firebase.**
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Keep Model Data Classes
-keep class com.akshar.dyslexiareader.data.local.entity.** { *; }
-keep class com.akshar.dyslexiareader.domain.model.** { *; }
