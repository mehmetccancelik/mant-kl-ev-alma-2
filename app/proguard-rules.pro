# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/user/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data classes
-keepclassmembers class com.evanaliz.core.** { *; }
-keepclassmembers class com.evanaliz.integration.** { *; }

# Keep accessibility service
-keep class com.evanaliz.accessibility.ScreenDataAccessibilityService { *; }
