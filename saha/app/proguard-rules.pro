# SAHA — keep Firebase / Maps model classes if minify is enabled later
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}
