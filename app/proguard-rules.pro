# Keep line numbers for Play Console crash deobfuscation.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Play Console looks for this API by name when checking edge-to-edge compatibility.
-keep class androidx.activity.EdgeToEdge {
    public static void enable(...);
}
