package ru.sage.pluginregistry.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PluginDefinition(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
)
