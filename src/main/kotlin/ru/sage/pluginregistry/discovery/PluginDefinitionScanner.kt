package ru.sage.pluginregistry.discovery

import org.springframework.beans.factory.findAnnotationOnBean
import org.springframework.beans.factory.getBeanNamesForAnnotation
import org.springframework.context.ApplicationContext
import ru.sage.pluginregistry.annotation.PluginDefinition

class PluginDefinitionScanner(
    private val applicationContext: ApplicationContext
) {
    fun scan(): PluginDefinition? {
        val definitions = applicationContext
            .getBeanNamesForAnnotation<PluginDefinition>()
            .mapNotNull { beanName ->
                applicationContext.findAnnotationOnBean<PluginDefinition>(beanName)
            }

        require(definitions.size <= 1) {
            "Expected at most one @PluginDefinition, found ${definitions.size}"
        }

        return definitions.singleOrNull()
    }
}