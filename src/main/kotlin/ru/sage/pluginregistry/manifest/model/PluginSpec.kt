package ru.sage.pluginregistry.manifest.model

data class PluginSpec(
    val endpoint: String,
    val capabilities: List<PluginCapability>
)
