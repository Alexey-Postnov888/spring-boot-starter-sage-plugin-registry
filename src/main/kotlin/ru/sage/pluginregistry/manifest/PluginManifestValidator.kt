package ru.sage.pluginregistry.manifest

import ru.sage.pluginregistry.manifest.exception.PluginManifestValidationException
import ru.sage.pluginregistry.manifest.model.PluginManifest
import java.net.URI

class PluginManifestValidator {
    fun validate(manifest: PluginManifest) {
        val errors = mutableListOf<String>()

        if (manifest.manifestVersion.isBlank()) {
            errors.add("manifestVersion must not be blank")
        }

        if (manifest.type.isBlank()) {
            errors.add("type must not be blank")
        }

        with(manifest.info) {
            if (id.isBlank()) {
                errors.add("info.id must not be blank")
            }

            if (name.isBlank()) {
                errors.add("info.name must not be blank")
            }

            if (version.isBlank()) {
                errors.add("info.version must not be blank")
            }
        }

        validateEndpoint(manifest.spec.endpoint, errors)

        manifest.spec.capabilities.forEachIndexed { index, capability ->
            if (capability.id.isBlank()) {
                errors.add("spec.capabilities[$index].id must not be blank")
            }

            if (capability.description.isBlank()) {
                errors.add("spec.capabilities[$index].description must not be blank")
            }
        }

        val duplicates = manifest.spec.capabilities
            .filter { it.id.isNotBlank() }
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        duplicates.forEach { id ->
            errors.add("duplicate capability ID: $id")
        }

        if (errors.isNotEmpty()) {
            throw PluginManifestValidationException(errors)
        }
    }

    private fun validateEndpoint(
        endpoint: String,
        errors: MutableList<String>
    ) {
        if (endpoint.isBlank()) {
            errors.add("spec.endpoint must not be blank")
            return
        }

        try {
            val uri = URI(endpoint)

            if (
                !endpoint.startsWith("/") ||
                uri.isAbsolute ||
                uri.rawAuthority != null ||
                uri.rawQuery != null ||
                uri.rawFragment != null
            ) {
                errors.add("spec.endpoint must be a relative HTTP path starting with '/'")
            }
        } catch (_: Exception) {
            errors.add("spec.endpoint must be a valid URI path")
        }
    }
}