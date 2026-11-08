package ru.sage.pluginregistry.discovery

import org.springframework.aop.support.AopUtils
import org.springframework.context.ApplicationContext
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import ru.sage.pluginregistry.annotation.PluginCapability
import ru.sage.pluginregistry.manifest.model.PluginCapability as PluginCapabilityManifest

class PluginCapabilityScanner(
    private val applicationContext: ApplicationContext
) {

    fun scan(): List<PluginCapabilityManifest> {
        val capabilities = applicationContext
            .beanDefinitionNames
            .flatMap { beanName ->
                val bean = applicationContext.getBean(beanName)
                val targetClass = AopUtils.getTargetClass(bean)

                MethodIntrospector.selectMethods(targetClass) { method ->
                    AnnotatedElementUtils.findMergedAnnotation(
                        method,
                        PluginCapability::class.java
                    ) != null
                }.mapNotNull { method ->
                    val annotation = AnnotatedElementUtils.findMergedAnnotation(
                        method,
                        PluginCapability::class.java
                    ) ?: return@mapNotNull null

                    PluginCapabilityManifest(
                        id = annotation.id,
                        description = annotation.description
                    )
                }
            }

        val duplicates = capabilities
            .groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys

        require(duplicates.isEmpty()) {
            "Duplicate plugin capability IDs: ${duplicates.joinToString()}"
        }

        return capabilities
    }
}