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

# Preserve metadata used by Retrofit/Gson and useful release stack traces.
-keepattributes Signature,*Annotation*,SourceFile,LineNumberTable

# Retrofit interfaces are accessed reflectively through generated proxies.
-keep interface com.yuch.snapcalfirebasegemini.data.api.ApiService { *; }

# Gson model fields are serialized/deserialized by name.
-keep class com.yuch.snapcalfirebasegemini.data.api.response.** { *; }
-keep class com.yuch.snapcalfirebasegemini.data.api.request.** { *; }
-keep class com.yuch.snapcalfirebasegemini.data.model.** { *; }

# Room database/DAO/entity classes are annotation-processed and may be reflected.
-keep class com.yuch.snapcalfirebasegemini.data.local.** { *; }

# OkHttp/Retrofit warning suppression recommended for release shrinking.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
