package ru.sage.pluginregistry.client.model.response

data class PluginResponse(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val manifestVersion: String
)
