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

# Custom rules for optimization (added by Hari)

# KotlinX Coroutines
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepnames class kotlinx.coroutines.flow.MutableStateFlow
-keepnames class kotlinx.coroutines.flow.StateFlow

# Jetpack Compose specific rules (general recommendations)
# Keep annotations for tooling like preview
-keepattributes Signature
-keepclassmembers,allowshrinking class * {
    @androidx.compose.ui.tooling.preview.Preview *;
}
-keepclassmembers class * {
    @kotlin.jvm.JvmDefault void <methods>(...);
}
-keepclassmembers class * {
    @kotlin.Metadata **.Companion;
}

# For Room library
-keepnames class * extends androidx.room.RoomDatabase
-keepnames class * implements androidx.room.Entity
-keepnames class * implements androidx.room.Dao
-keepclassmembers class ** {
    @androidx.room.* <methods>;
}
