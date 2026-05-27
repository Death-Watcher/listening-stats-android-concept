-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.listeningstats.app.data.api.dto.**$$serializer { *; }
-keepclassmembers class com.listeningstats.app.data.api.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.listeningstats.app.data.api.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}
