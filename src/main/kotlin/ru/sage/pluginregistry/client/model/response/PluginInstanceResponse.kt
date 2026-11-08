package ru.sage.pluginregistry.client.model.response

import ru.sage.pluginregistry.client.model.PluginInstanceStatus
import java.util.UUID

data class PluginInstanceResponse(
    val id: UUID,
    val pluginId: String,
    val endpoint: String,
    val status: PluginInstanceStatus
)
