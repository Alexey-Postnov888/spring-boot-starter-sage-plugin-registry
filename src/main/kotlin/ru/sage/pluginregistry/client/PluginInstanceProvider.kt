package ru.sage.pluginregistry.client

import java.util.UUID

interface PluginInstanceProvider {
    val instanceId: UUID?
    fun setInstanceId(instanceId: UUID)
}