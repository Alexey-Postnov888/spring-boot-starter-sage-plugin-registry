package ru.sage.pluginregistry.lifecycle

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import ru.sage.pluginregistry.client.PluginInstanceProvider
import ru.sage.pluginregistry.client.RegistryClient
import ru.sage.pluginregistry.client.mapper.toDto
import ru.sage.pluginregistry.client.model.dto.ManifestDto
import ru.sage.pluginregistry.client.model.response.PluginInstanceResponse
import ru.sage.pluginregistry.configuration.Registration
import ru.sage.pluginregistry.manifest.PluginManifestProvider
import kotlin.math.pow

class PluginRegistrationRunner(
    private val registryClient: RegistryClient,
    private val manifestProvider: PluginManifestProvider,
    private val instanceProvider: PluginInstanceProvider,
    private val registration: Registration
) : ApplicationRunner {
    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments) {
        val manifest = manifestProvider.provide()

        try {
            val instance = registerWithRetry(manifest.toDto())

            instanceProvider.setInstanceId(instance.id)

            logger.info { "Plugin '${instance.pluginId}' successfully registered." +
                    " Instance ID: ${instance.id}" }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw e
        } catch (e: Exception) {
            if (registration.failFast) throw e

            logger.debug(e) {
                "Plugin registration failure details."
            }

            logger.warn {
                "Plugin registration failed after ${registration.retry.maxAttempts} attempts. " +
                        "Application startup will continue without registration."
            }

        }
    }

    private fun registerWithRetry(manifest: ManifestDto): PluginInstanceResponse {
        val retry = registration.retry
        var attempt = 1

        while (true) {
            try {
                return registryClient.registerPlugin(manifest)
            } catch (e: Exception) {
                if (!isRetryable(e) || attempt >= retry.maxAttempts) {
                    throw e
                }

                val delay = (
                        retry.initialDelay.toMillis() *
                                retry.multiplier.pow(attempt - 1)
                        ).toLong()

                logger.warn {
                    "Plugin registration attempt $attempt/${retry.maxAttempts} failed. " +
                            "Retrying in ${delay} ms."
                }

                Thread.sleep(delay)
                attempt++
            }
        }
    }

    private fun isRetryable(e: Exception): Boolean =
        e is ResourceAccessException ||
                e is HttpServerErrorException ||
                (e is HttpClientErrorException && e.statusCode.value() == 429)
}