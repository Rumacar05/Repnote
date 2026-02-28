# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ===========================
# General Android/Kotlin Rules
# ===========================

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signature for reflection and serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep Kotlin Metadata for reflection
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# ===========================
# Room Database Rules
# ===========================

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room DAO interfaces and implementations
-keep interface * extends androidx.room.Dao
-keep class * extends androidx.room.Dao

# Keep Room entities (database tables)
-keep class com.ruma.repnote.core.database.entity.** { *; }

# Keep Room DAOs
-keep class com.ruma.repnote.core.database.dao.** { *; }

# Keep Room TypeConverters
-keep class com.ruma.repnote.core.database.converter.** { *; }

# Keep Room database
-keep class com.ruma.repnote.core.database.RepnoteDatabase { *; }

# Keep all @Entity annotated classes
-keep @androidx.room.Entity class * { *; }

# ===========================
# Firebase/Firestore Rules
# ===========================

# CRITICAL: Keep ALL Firestore document model classes completely intact
# Firestore uses reflection to deserialize data, so fields, methods, and constructors must not be obfuscated
-keep class com.ruma.repnote.core.data.model.** { *; }

# Keep @PropertyName annotations (Firestore uses these for field mapping)
-keepattributes *Annotation*
-keepclassmembers class com.ruma.repnote.core.data.model.** {
    @com.google.firebase.firestore.PropertyName *;
}

# Keep empty constructors (required for Firestore deserialization)
-keepclassmembers class com.ruma.repnote.core.data.model.** {
    public <init>();
}

# Keep getters and setters (Firestore uses reflection to access them)
-keepclassmembers class com.ruma.repnote.core.data.model.** {
    public *** get*();
    public *** is*();
    public void set*(***);
}

# Keep all domain model classes (used in app logic and Firestore mapping)
-keep class com.ruma.repnote.core.domain.model.** { *; }

# Keep Firestore-specific classes
-keep class com.google.firebase.firestore.** { *; }
-keep interface com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Keep model classes used with Firestore's toObject() method
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.Exclude <fields>;
}

# ===========================
# Hilt/Dagger Rules
# ===========================

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt-generated modules
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }

# Keep @Inject constructors
-keepclasseswithmembernames class * {
    @javax.inject.Inject <init>(...);
}

# Keep @Inject fields
-keepclasseswithmembernames class * {
    @javax.inject.Inject <fields>;
}

# Keep @Inject methods
-keepclasseswithmembernames class * {
    @javax.inject.Inject <methods>;
}

# Keep Hilt entry points
-keep @dagger.hilt.android.EntryPoint class * { *; }

# ===========================
# Jetpack Compose Rules
# ===========================

# Keep Composable functions (critical for Compose to work)
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep classes that use Compose remember
-keepclassmembers class * {
    @androidx.compose.runtime.Stable <methods>;
}

# Prevent obfuscation of Compose UI components
-keep class androidx.compose.** { *; }

# ===========================
# Navigation Component
# ===========================

# Keep navigation arguments
-keepclassmembers class * extends androidx.navigation.Navigator {
    <init>(...);
}

# Keep navigation3 classes
-keep class androidx.navigation3.** { *; }

# ===========================
# Kotlin Serialization
# ===========================

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep serializers
-keep,includedescriptorclasses class com.ruma.repnote.**$$serializer { *; }

# Keep companion objects for serialization
-keepclassmembers class com.ruma.repnote.** {
    *** Companion;
}

# Keep serializer methods
-keepclasseswithmembers class com.ruma.repnote.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @SerialName annotated fields
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ===========================
# Kotlin Coroutines
# ===========================

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep volatile fields for coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep continuation classes
-keep class kotlin.coroutines.Continuation
-keep class kotlinx.coroutines.** { *; }

# ===========================
# Cloudinary
# ===========================

-keep class com.cloudinary.** { *; }
-dontwarn com.cloudinary.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient

# ===========================
# Coil Image Loading
# ===========================

-keep class coil3.** { *; }
-keep interface coil3.** { *; }
-dontwarn coil3.**

# Keep Coil network implementations
-keep class coil3.network.okhttp.** { *; }

# ===========================
# OkHttp & Retrofit (used by Coil)
# ===========================

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ===========================
# Android Resources
# ===========================

# Keep R classes
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# Keep resource IDs
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ===========================
# Data Classes & Sealed Classes
# ===========================

# Keep data class copy methods
-keepclassmembers class * {
    public ** copy(...);
}

# Keep sealed class constructors
-keep class * extends kotlin.Enum { *; }

# ===========================
# Enum Classes
# ===========================

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
    <init>(...);
}

# ===========================
# Parcelable
# ===========================

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ===========================
# ViewModel & Lifecycle
# ===========================

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep SavedStateHandle constructors
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(androidx.lifecycle.SavedStateHandle);
}

# ===========================
# Reflection & Annotation Processing
# ===========================

# Keep classes with @Keep annotation
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ===========================
# Optimization: Remove Logging
# ===========================

# Remove verbose logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove Timber logging (if added later)
-assumenosideeffects class timber.log.Timber* {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ===========================
# Crashlytics (if enabled later)
# ===========================

# Keep file names and line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

# Keep custom exceptions
-keep class * extends java.lang.Exception { *; }

# ===========================
# General Warnings to Suppress
# ===========================

-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler

# ===========================
# Play Services & Credentials
# ===========================

-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# ===========================
# Optional: Aggressive Optimizations
# (Uncomment if build size is critical)
# ===========================

# -optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# -optimizationpasses 5
# -allowaccessmodification
# -dontpreverify