package ru.sage.pluginregistry.manifest.model

data class PluginManifest(
    val manifestVersion: String,
    val type: String,
    val info: PluginInfo,
    val spec: PluginSpec
)
