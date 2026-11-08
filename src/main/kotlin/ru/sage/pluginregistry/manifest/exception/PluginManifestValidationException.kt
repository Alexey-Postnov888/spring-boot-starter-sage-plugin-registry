package ru.sage.pluginregistry.manifest.exception

class PluginManifestValidationException(
    errors: List<String>
) : IllegalArgumentException(
    "Invalid plugin manifest:\n" +
            errors.joinToString("\n") { "- $it" }
)