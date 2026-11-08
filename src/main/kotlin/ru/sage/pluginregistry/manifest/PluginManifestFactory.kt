package ru.sage.pluginregistry.manifest

import ru.sage.pluginregistry.configuration.PluginRegistryProperties
import ru.sage.pluginregistry.discovery.PluginCapabilityScanner
import ru.sage.pluginregistry.discovery.PluginDefinitionScanner
import ru.sage.pluginregistry.manifest.model.PluginCapability
import ru.sage.pluginregistry.manifest.model.PluginInfo
import ru.sage.pluginregistry.manifest.model.PluginManifest
import ru.sage.pluginregistry.manifest.model.PluginSpec

class PluginManifestFactory(
    private val properties: PluginRegistryProperties,
    private val definitionScanner: PluginDefinitionScanner,
    private val capabilityScanner: PluginCapabilityScanner,
    private val validator: PluginManifestValidator
) {
    fun create(): PluginManifest {
        val yaml = properties.manifest
        val definition = definitionScanner.scan()

        val yamlCapabilities =yaml.spec.capabilities.map {
            PluginCapability(
                id = it.id,
                description = it.description
            )
        }

        val annotationCapabilities = capabilityScanner.scan()

        val manifest = PluginManifest(
            manifestVersion = yaml.manifestVersion ?: MANIFEST_VERSION,
            type = yaml.type ?: PLUGIN_TYPE,
            info = PluginInfo(
                id = definition?.id ?: yaml.info.id.orEmpty(),
                version = definition?.version ?: yaml.info.version.orEmpty(),
                name = definition?.name ?: yaml.info.name.orEmpty(),
                description = definition?.description
                    ?.takeIf { it.isNotBlank() }
                    ?: yaml.info.description.orEmpty()
            ),
            spec = PluginSpec(
                endpoint = yaml.spec.endpoint ?: DEFAULT_ENDPOINT,
                capabilities = yamlCapabilities + annotationCapabilities
            )
        )

        validator.validate(manifest)

        return manifest
    }

    private companion object {
        const val MANIFEST_VERSION = "v1"
        const val PLUGIN_TYPE = "Plugin"
        const val DEFAULT_ENDPOINT = "/commands"
    }
}