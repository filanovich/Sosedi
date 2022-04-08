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

-dontwarn org.apache.**
-dontwarn com.zebra.**
-dontwarn com.fasterxml.**

-dontwarn  by.imlab.data.api.model.**

-keep class org.apache.** { *; }
-keep interface org.apache.** { *; }
-keep class com.zebra.** { *; }
-keep interface com.zebra.** { *; }
-keep class com.fasterxml.** { *; }
-keep interface com.fasterxml.** { *; }

-keep class by.imlab.data.api.model.** {*;}
-keep interface by.imlab.data.api.model.** {*;}
