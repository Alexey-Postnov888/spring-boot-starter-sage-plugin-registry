package ru.sage.pluginregistry.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import ru.sage.pluginregistry.client.PluginInstanceProvider
import ru.sage.pluginregistry.client.RegistryClient
import ru.sage.pluginregistry.discovery.PluginCapabilityScanner
import ru.sage.pluginregistry.discovery.PluginDefinitionScanner
import ru.sage.pluginregistry.lifecycle.DefaultPluginInstanceProvider
import ru.sage.pluginregistry.lifecycle.PluginHeartbeatScheduler
import ru.sage.pluginregistry.lifecycle.PluginRegistrationRunner
import ru.sage.pluginregistry.lifecycle.PluginUnregistrationListener
import ru.sage.pluginregistry.manifest.PluginManifestFactory
import ru.sage.pluginregistry.manifest.PluginManifestProvider
import ru.sage.pluginregistry.manifest.PluginManifestValidator
import java.net.http.HttpClient

@AutoConfiguration
@EnableConfigurationProperties(PluginRegistryProperties::class)
@ConditionalOnProperty(
    prefix = "sage.plugin-registry",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class PluginRegistryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun pluginDefinitionScanner(
        applicationContext: ApplicationContext
    ) = PluginDefinitionScanner(applicationContext)

    @Bean
    @ConditionalOnMissingBean
    fun pluginCapabilityScanner(
        applicationContext: ApplicationContext
    ) = PluginCapabilityScanner(applicationContext)

    @Bean
    @ConditionalOnMissingBean
    fun pluginManifestValidator() = PluginManifestValidator()

    @Bean
    @ConditionalOnMissingBean
    fun pluginManifestFactory(
        properties: PluginRegistryProperties,
        definitionScanner: PluginDefinitionScanner,
        capabilityScanner: PluginCapabilityScanner,
        validator: PluginManifestValidator
    ) = PluginManifestFactory(
        properties,
        definitionScanner,
        capabilityScanner,
        validator
    )

    @Bean
    @ConditionalOnMissingBean(PluginManifestProvider::class)
    fun pluginManifestProvider(
        factory: PluginManifestFactory
    ): PluginManifestProvider = PluginManifestProvider {
        factory.create()
    }

    @Bean("registryRestClient")
    @ConditionalOnMissingBean(name = ["registryRestClient"])
    @ConditionalOnProperty(
        prefix = "sage.plugin-registry.registration",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun registryRestClient(
        properties: PluginRegistryProperties
    ): RestClient {
         val httpClient = HttpClient.newBuilder()
             .connectTimeout(properties.connectionTimeout)
             .build()

        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(properties.readTimeout)
        }

        return RestClient.builder()
            .requestFactory(requestFactory)
            .build()
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "sage.plugin-registry.registration",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun registryClient(
        @Qualifier("registryRestClient") restClient: RestClient,
        properties: PluginRegistryProperties
    ) = RegistryClient(
        restClient = restClient,
        registryUrl = requireNotNull(properties.url) {
            "Property 'sage.plugin-registry.url' must be configured"
        }.trimEnd('/')
    )

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "sage.plugin-registry.registration",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun pluginRegistrationRunner(
        registryClient: RegistryClient,
        manifestProvider: PluginManifestProvider,
        instanceProvider: PluginInstanceProvider,
        properties: PluginRegistryProperties
    ) = PluginRegistrationRunner(
        registryClient,
        manifestProvider,
        instanceProvider,
        registration = properties.registration,
    )

    @Bean
    @ConditionalOnMissingBean(PluginInstanceProvider::class)
    fun pluginInstanceProvider(): PluginInstanceProvider = DefaultPluginInstanceProvider()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "sage.plugin-registry.registration",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnProperty(
        prefix = "sage.plugin-registry.heartbeat",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun pluginHeartbeatScheduler(
        registryClient: RegistryClient,
        instanceProvider: PluginInstanceProvider,
        properties: PluginRegistryProperties
    ) = PluginHeartbeatScheduler(
        registryClient = registryClient,
        instanceProvider = instanceProvider,
        interval = properties.heartbeat.interval
    )

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "sage.plugin-registry.registration",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun pluginUnregistrationListener(
        registryClient: RegistryClient,
        instanceProvider: PluginInstanceProvider
    ) = PluginUnregistrationListener(
        registryClient = registryClient,
        instanceProvider = instanceProvider
    )
}