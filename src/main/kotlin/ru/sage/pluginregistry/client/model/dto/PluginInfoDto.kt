package ru.sage.pluginregistry.client.model.dto

data class PluginInfoDto(
    val id: String,
    val version: String,
    val name: String,
    val description: String
)
