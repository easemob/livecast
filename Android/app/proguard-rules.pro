
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\work\android_sdk\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.hyphenate.** {*;}
-dontwarn  com.hyphenate.**
//3.6.8版本之后移除apache，无需再添加
-keep class internal.org.apache.http.entity.** {*;}
//如果使用了实时音视频功能
-keep class com.superrtc.** {*;}
-dontwarn  com.superrtc.**