package ru.sage.pluginregistry.client

import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import ru.sage.pluginregistry.client.model.dto.ManifestDto
import ru.sage.pluginregistry.client.model.response.PluginInstanceResponse
import java.util.UUID

class RegistryClient(
    private val restClient: RestClient,
    private val registryUrl: String
) {
    fun registerPlugin(manifest: ManifestDto): PluginInstanceResponse =
        restClient.post()
            .uri("$registryUrl/api/v1/plugins/register")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(manifest)
            .retrieve()
            .body<PluginInstanceResponse>()
            ?: error("Failed to register plugin")

    fun sendHeartbeat(instanceId: UUID) {
        restClient.post()
            .uri("$registryUrl/api/v1/plugin-instances/$instanceId/heartbeat")
            .retrieve()
            .toBodilessEntity()
    }

    fun unregisterInstance(instanceId: UUID) {
        restClient.delete()
            .uri("$registryUrl/api/v1/plugin-instances/$instanceId")
            .retrieve()
            .toBodilessEntity()
    }
}