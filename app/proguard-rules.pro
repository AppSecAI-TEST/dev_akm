# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in e:\Android\sdk/tools/proguard/proguard-android.txt
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
-optimizationpasses 5                                                       #指定代码压缩级别
-dontusemixedcaseclassnames                                                #混淆时不会产生形形色色的类名
-dontskipnonpubliclibraryclasses                                          #指定不忽略非公共类库
-dontpreverify                                                              #不预校验，如果需要预校验，是-dontoptimize
-ignorewarnings                                                             #屏蔽警告
-verbose                                                                     #混淆时记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    #优化


-dontwarn android.support.v4.**                                             #去掉警告
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment
-keep class android-support-v4.**{*;}

-keep public class * extends android.support.v7.**

-keep class com.google.zxing.**{*;}
-keep class com.google.gson.**{*;}
-keep class com.yolanda.nohttp.**{*;}
-keep class org.apache.http.**{*;}
-keep class org.apache.http.entity.mime.**{*;}
-keep class org.codehaus,jackson.**{*;}
-keep class com.rabbitmq.**{*;}
-keep class butterknife.internal.**{*;}
-keep class com.bumptech.glide.**{*;}
-keep class jp.wasabeef.glide.transformations.**{*;}
-keep class io.realm.**{*;}
-keep class org.apache.http.**{*;}
-keep class org.slf4j.**{*;}
-keep class com.bumptech.qlide.**{*;}
-keep class com.zongsheng.drink.h17.background.bean.**{*;}
-keep class com.zongsheng.drink.h17.front.bean.**{*;}
-keep class com.zongsheng.drink.h17.common.**{*;}
-keep class com.bigkoo.alertview.**{*;}


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService


-keepclassmembers class * implements java.io.Serializable {
        static final long serialVersionUID;
        private static final java.io.ObjectStreamField[] serialPersistentFields;
        private void writeObject(java.io.ObjectOutputStream);
        private void readObject(java.io.ObjectInputStream);
        java.lang.Object writeReplace();
        java.lang.Object readResolve();
}


-keepclasseswithmembernames class * {                                           # 保持 native 方法不被混淆
    native <methods>;
}
-keepclassmembers enum * {                                                      # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {                                # 保持 Parcelable 不被混淆
  public static final android.os.Parcelable$Creator *;
}