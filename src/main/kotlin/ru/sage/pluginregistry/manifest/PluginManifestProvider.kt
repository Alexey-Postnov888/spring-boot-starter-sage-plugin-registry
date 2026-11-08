package ru.sage.pluginregistry.manifest

import ru.sage.pluginregistry.manifest.model.PluginManifest

fun interface PluginManifestProvider {
    fun provide(): PluginManifest
}