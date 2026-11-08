package ru.sage.pluginregistry.lifecycle

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import ru.sage.pluginregistry.client.PluginInstanceProvider
import ru.sage.pluginregistry.client.RegistryClient
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PluginHeartbeatScheduler(
    private val registryClient: RegistryClient,
    private val instanceProvider: PluginInstanceProvider,
    private val interval: Duration
) {
    private val logger = KotlinLogging.logger {}

    private val executor = Executors.newSingleThreadScheduledExecutor()

    private var heartbeatTask: ScheduledFuture<*>? = null
    private var heartbeatFailed = false

    init {
        require(
            !interval.isNegative &&
                    !interval.isZero &&
                    interval.toMillis() > 0
        ) {
            "Property 'sage.plugin-registry.heartbeat.interval' must be greater than zero"
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        val instanceId = instanceProvider.instanceId ?: return

        heartbeatTask = executor.scheduleWithFixedDelay(
            {
                try {
                    registryClient.sendHeartbeat(instanceId)

                    if (heartbeatFailed) {
                        heartbeatFailed = false
                        logger.info {
                            "Heartbeat connection restored for instance '$instanceId'"
                        }
                    }
                } catch (e: Exception) {
                    if (!heartbeatFailed) {
                        heartbeatFailed = true
                        logger.warn {
                            "Failed to send heartbeat for instance '$instanceId'. " +
                                    "Will retry on the next scheduled execution."
                        }
                    }
                }
            },
            0,
            interval.toMillis(),
            TimeUnit.MILLISECONDS
        )

        logger.info {
            "Heartbeat started for instance '$instanceId' with interval: ${interval.toMillis()}ms"
        }
    }

    @EventListener(ContextClosedEvent::class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun stop() {
        executor.shutdown()

        if (heartbeatTask != null) {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }

            logger.info { "Heartbeat scheduler stopped" }
        }
    }
}