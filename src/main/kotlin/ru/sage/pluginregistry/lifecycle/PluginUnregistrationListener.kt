package ru.sage.pluginregistry.lifecycle

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import ru.sage.pluginregistry.client.PluginInstanceProvider
import ru.sage.pluginregistry.client.RegistryClient

class PluginUnregistrationListener(
    private val registryClient: RegistryClient,
    private val instanceProvider: PluginInstanceProvider
) {
    private val logger = KotlinLogging.logger {}

    @EventListener(ContextClosedEvent::class)
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun unregister() {
        val instanceId = instanceProvider.instanceId ?: return

        try {
            registryClient.unregisterInstance(instanceId)

            logger.info {
                "Plugin instance '$instanceId' successfully unregistered"
            }
        } catch (e: Exception) {
            logger.warn {
                "Failed to unregister plugin instance '$instanceId'. " +
                        "It will be marked as unavailable after heartbeat timeout."
            }
        }
    }
}