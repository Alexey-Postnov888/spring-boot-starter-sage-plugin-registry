package ru.sage.pluginregistry.client.model.dto

data class ManifestDto(
    val manifestVersion: String,
    val type: String,
    val info: PluginInfoDto,
    val spec: PluginSpecDto
)