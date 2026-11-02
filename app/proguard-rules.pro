# Add project specific ProGuard rules here.
-keep class com.example.cloneapp.models.** { *; }
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# VirtualApp engine
-keep class com.lody.virtual.** { *; }
-keep class mirror.** { *; }
-keep class android.** { *; }
