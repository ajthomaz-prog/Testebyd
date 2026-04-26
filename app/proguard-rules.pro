# Keep kotlinx.serialization metadata for our DTOs
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.bydnews.briefing.net.**Dto** { *; }
-keep class com.bydnews.briefing.net.Anthropic* { *; }
-keep class com.bydnews.briefing.net.GoogleTts* { *; }
-keep class com.bydnews.briefing.data.model.** { *; }
