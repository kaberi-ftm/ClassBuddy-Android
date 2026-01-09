# Add project specific ProGuard rules here.

# Keep ClassBuddy models
-keep class com.classbuddy. app.data.model.** { *; }

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class com.google.firebase.** { *; }
-keep class com. google.android.gms.** { *; }

# Firestore
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com. classbuddy.app. data.model.** {
    *;
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}

# BCrypt
-keep class org.mindrot. jbcrypt. ** { *; }

# Lottie
-dontwarn com.airbnb.lottie.**
-keep class com. airbnb.lottie.** { *; }

# Navigation Component
-keepnames class androidx.navigation.fragment.NavHostFragment

# ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}