# Add project specific ProGuard rules here.
# Keep Compose runtime metadata; the Compose compiler handles most shrinking concerns.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep data model classes used for local persistence.
-keep class com.liman.app.data.model.** { *; }
