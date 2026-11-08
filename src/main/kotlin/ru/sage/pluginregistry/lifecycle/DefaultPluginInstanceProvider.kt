package ru.sage.pluginregistry.lifecycle

import ru.sage.pluginregistry.client.PluginInstanceProvider
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class DefaultPluginInstanceProvider : PluginInstanceProvider {
    private val instanceIdReference = AtomicReference<UUID?>(null)

    override val instanceId: UUID?
        get() = instanceIdReference.get()

    override fun setInstanceId(instanceId: UUID) {
        instanceIdReference.set(instanceId)
    }
}