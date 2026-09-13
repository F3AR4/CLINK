# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in <ANDROID_SDK>/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Keep Hilt generated classes
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.internal.TestSingletonComponent { *; }
