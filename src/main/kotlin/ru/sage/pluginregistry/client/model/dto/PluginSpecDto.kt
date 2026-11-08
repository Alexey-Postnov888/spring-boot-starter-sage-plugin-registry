package ru.sage.pluginregistry.client.model.dto

data class PluginSpecDto(
    val endpoint: String,
    val capabilities: List<PluginCapabilityDto>
)
