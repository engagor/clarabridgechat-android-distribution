# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in android-sdk/tools/proguard/proguard-android.txt
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

# Common attributes
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod, *Annotation*

# Firebase
-dontnote com.google.firebase.**

# OkHttp
-dontwarn okhttp3.**
-dontnote okhttp3.**

# Retrofit
-dontwarn retrofit2.HttpServiceMethod*
-dontwarn retrofit2.RequestFactory*

# Gson
-dontwarn sun.misc.**
-dontnote com.google.gson.internal.**

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# com.clarabridge.core
-keepclassmembers enum com.clarabridge.core.** { *; }
-keep public class com.clarabridge.core.AbstractNotificationReceiver { public *; }
-keep public class com.clarabridge.core.ActionState { public *; }
-keep public interface com.clarabridge.core.AuthenticationCallback { *; }
-keep public interface com.clarabridge.core.AuthenticationDelegate { *; }
-keep public class com.clarabridge.core.AuthenticationError { public *; }
-keep public class com.clarabridge.core.CardSummary { public *; }
-keep public class com.clarabridge.core.Config { public *; }
-keep public interface com.clarabridge.core.Conversation { *; }
-keep public interface com.clarabridge.core.ConversationDelegate { *; }
-keep public interface com.clarabridge.core.ConversationDetails { *; }
-keep public class com.clarabridge.core.ConversationEvent { public *; }
-keep public enum com.clarabridge.core.ConversationEventType { public *; }
-keep public interface com.clarabridge.core.ConversationViewDelegate { *; }
-keep public class com.clarabridge.core.Coordinates { public *; }
-keep public class com.clarabridge.core.CreditCard { public *; }
-keep public class com.clarabridge.core.DisplaySettings { public *; }
-keep public class com.clarabridge.core.FcmService { public *; }
-keep public enum com.clarabridge.core.InitializationStatus { public *; }
-keep public class com.clarabridge.core.Integration { public *; }
-keep public class com.clarabridge.core.Logger { public *; }
-keep public enum com.clarabridge.core.LoginResult { public *; }
-keep public enum com.clarabridge.core.LogoutResult { public *; }
-keep public class com.clarabridge.core.Message { public *; }
-keep public class com.clarabridge.core.MessageAction { public *; }
-keep public class com.clarabridge.core.MessageItem { public *; }
-keep public interface com.clarabridge.core.MessageModifierDelegate { *; }
-keep public class com.clarabridge.core.MessageType { public *; }
-keep public enum com.clarabridge.core.MessageUploadStatus { public *; }
-keep public class com.clarabridge.core.Participant { public *; }
-keep public enum com.clarabridge.core.PaymentStatus { public *; }
-keep public class com.clarabridge.core.Settings { public *; }
-keep public class com.clarabridge.core.ClarabridgeChat { public *; }
-keep public interface com.clarabridge.core.ClarabridgeChatCallback { public *; }
-keep public class com.clarabridge.core.ClarabridgeChatCallback$Response { public *; }
-keep public enum com.clarabridge.core.ClarabridgeChatConnectionStatus { public *; }
-keep public class com.clarabridge.core.User { public *; }
-keep public class com.clarabridge.core.utils.FileUtils { public *; }
-keep public class com.clarabridge.core.utils.StringUtils { public *; }

-dontnote com.clarabridge.core.Notifier

# com.clarabridge.core.service
-keep public class com.clarabridge.core.service.ClarabridgeChatService

# com.clarabridge.core.model
-keep class com.clarabridge.core.model.** { <fields>; }
-keepclassmembers enum com.clarabridge.core.model.** { *; }

-keep class com.clarabridge.core.monitor.WsActivityDto { <fields>; }
-keep class com.clarabridge.core.monitor.WsClientDto { <fields>; }
-keep class com.clarabridge.core.monitor.WsConversationDto { <fields>; }
-keep class com.clarabridge.core.monitor.WsErrorDataDto { <fields>; }
-keep class com.clarabridge.core.monitor.WsErrorDto { <fields>; }
-keep class com.clarabridge.core.monitor.WsMessageDto { <fields>; }
-keep class com.clarabridge.core.monitor.WsParticipantDto { <fields>; }
