package ru.sage.pluginregistry.client.mapper

import ru.sage.pluginregistry.client.model.dto.ManifestDto
import ru.sage.pluginregistry.client.model.dto.PluginCapabilityDto
import ru.sage.pluginregistry.client.model.dto.PluginInfoDto
import ru.sage.pluginregistry.client.model.dto.PluginSpecDto
import ru.sage.pluginregistry.manifest.model.PluginManifest

fun PluginManifest.toDto(): ManifestDto = ManifestDto(
    manifestVersion = manifestVersion,
    type = type,
    info = PluginInfoDto(
        id = info.id,
        version = info.version,
        name = info.name,
        description = info.description
    ),
    spec = PluginSpecDto(
        endpoint = spec.endpoint,
        capabilities = spec.capabilities.map {
            PluginCapabilityDto(
                id = it.id,
                description = it.description
            )
        }
    )
)