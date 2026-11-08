package ru.sage.pluginregistry.configuration

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.validation.annotation.Validated
import java.time.Duration

@Validated
@ConfigurationProperties(prefix = "sage.plugin-registry")
data class PluginRegistryProperties(
    var enabled: Boolean = true,
    var url: String? = null,
    var connectionTimeout: Duration = Duration.ofSeconds(3),
    var readTimeout: Duration = Duration.ofSeconds(5),

    @field:Valid
    @NestedConfigurationProperty
    var registration: Registration = Registration(),

    @field:Valid
    @NestedConfigurationProperty
    var heartbeat: Heartbeat = Heartbeat(),

    @NestedConfigurationProperty
    var manifest: Manifest = Manifest()
) {
    @get:AssertTrue(
        message = "url must be configured when registration is enabled"
    )
    val registryUrlValid: Boolean
        get() = !registration.enabled || !url.isNullOrBlank()

    @get:AssertTrue(message = "connection-timeout must be greater than zero")
    val connectionTimeoutValid: Boolean
        get() = !connectionTimeout.isNegative && !connectionTimeout.isZero

    @get:AssertTrue(message = "read-timeout must be greater than zero")
    val readTimeoutValid: Boolean
        get() = !readTimeout.isNegative && !readTimeout.isZero

    @get:AssertTrue(
        message = "heartbeat.enabled requires registration.enabled"
    )
    val heartbeatRequiresRegistrationValid: Boolean
        get() = !heartbeat.enabled || registration.enabled
}

data class Registration(
    var enabled: Boolean = true,
    var failFast: Boolean = false,

    @field:Valid
    @NestedConfigurationProperty
    var retry: Retry = Retry()
)

data class Heartbeat(
    var enabled: Boolean = true,
    var interval: Duration = Duration.ofSeconds(15)
) {
    @get:AssertTrue(message = "heartbeat.interval must be greater than zero")
    val intervalValid: Boolean
        get() = !interval.isNegative && !interval.isZero
}

data class Manifest(
    var manifestVersion: String? = null,
    var type: String? = null,
    @NestedConfigurationProperty
    var info: Info = Info(),
    @NestedConfigurationProperty
    var spec: Spec = Spec()
)

data class Info(
    var id: String? = null,
    var name: String? = null,
    var version: String? = null,
    var description: String? = null
)

data class Spec(
    var endpoint: String? = null,
    var capabilities: MutableList<Capability> = mutableListOf()
)

data class Capability(
    var id: String = "",
    var description: String = ""
)

data class Retry(
    @field:Min(1, message = "registration.retry.max-attempts must be at least 1")
    var maxAttempts: Int = 3,
    var initialDelay: Duration = Duration.ofSeconds(1),
    var multiplier: Double = 2.0
) {
    @get:AssertTrue(message = "registration.retry.initial-delay must not be negative")
    val initialDelayValid: Boolean
        get() = !initialDelay.isNegative

    @get:AssertTrue(message = "registration.retry.multiplier must be finite and at least 1.0")
    val multiplierValid: Boolean
        get() = multiplier.isFinite() && multiplier >= 1.0
}
