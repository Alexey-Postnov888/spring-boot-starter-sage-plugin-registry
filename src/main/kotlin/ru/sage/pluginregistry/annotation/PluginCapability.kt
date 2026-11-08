package ru.sage.pluginregistry.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PluginCapability(
    val id: String,
    val description: String,
)