package com.ragin.bdd.cucumber.rest.utils

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.DEFAULT_HOST
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.DEFAULT_MANAGEMENT_WEB_BASE_PATH
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.LOCAL_PORT_PATTERN
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.PLACEHOLDER_PREFIX
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.PROPERTY_MANAGEMENT_BASE_PATH
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.PROPERTY_MANAGEMENT_WEB_BASE_PATH
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.PROPERTY_SERVLET_CONTEXT_PATH
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.PROPERTY_WEBFLUX_BASE_PATH
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.SERVICE_NAME_MANAGEMENT
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.Services.SERVICE_NAME_SERVER
import com.ragin.bdd.cucumber.rest.constants.CucumberRestConstants.UrlPath.HTTP_PROTOCOL
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.Environment

/**
 * Resolves which web server of the application under test a relative path belongs to.
 *
 * Spring Boot registers a `local.<namespace>.port` property for every web server it starts, so
 * scanning the environment for those properties enumerates all of them under the names Spring
 * itself uses (`server` for the main web server, `management` for the actuator child context).
 * Anything found this way can be overridden or extended through `cucumbertest.services`.
 */
class ServiceUrlResolver(private val environment: Environment, private val bddProperties: BddProperties) {
    /**
     * Determines the service a request should be sent to.
     *
     * @param relativePath  the requested path including the URL base path, with all placeholders
     *                      already replaced
     * @param explicitServiceName  service that the scenario selected explicitly, or `null`
     * @return the service to use, or `null` when the caller should keep its current behaviour
     * @throws IllegalArgumentException when [explicitServiceName] names an unknown service, or when
     *                                 the selected service still holds an unresolved placeholder
     */
    fun resolveFor(relativePath: String, explicitServiceName: String?): ResolvedService? {
        val services = knownServices()

        if (explicitServiceName.isNullOrBlank()) {
            return serviceForPath(relativePath = relativePath, services = services)?.also { service ->
                requireResolved(service = service)
            }
        }

        val service = services[explicitServiceName]
        requireNotNull(service) {
            "Unknown service '$explicitServiceName'. Known services: " +
                if (services.isEmpty()) "<none>" else services.keys.sorted().joinToString()
        }
        requireResolved(service = service)

        return service
    }

    /**
     * Fails when a placeholder of the selected service could not be resolved.
     *
     * Checked only for the service that is actually used, so an unrelated service with a
     * placeholder that never resolves cannot break a scenario that does not address it.
     */
    private fun requireResolved(service: ResolvedService) {
        val unresolved = listOf(service.host, service.port, service.basePath)
            .filterNotNull()
            .filter { value -> value.contains(other = PLACEHOLDER_PREFIX) }

        require(unresolved.isEmpty()) {
            "Service '${service.name}' has unresolved placeholders: ${unresolved.joinToString()}. " +
                "The referenced properties are not present in the Spring environment."
        }
    }

    /**
     * All services that are currently addressable, discovered ones merged with configured ones.
     */
    fun knownServices(): Map<String, ResolvedService> {
        val discoveredPorts = discoverPorts()
        val names = discoveredPorts.keys + bddProperties.services.keys

        return names.associateWith { name ->
            mergedService(
                name = name,
                discoveredPort = discoveredPorts[name],
                configured = bddProperties.services[name]
            )
        }
    }

    /**
     * Reads every `local.<namespace>.port` property from the environment.
     *
     * Property sources are ordered by precedence, so the first hit for a namespace wins.
     */
    private fun discoverPorts(): Map<String, String> {
        val configurableEnvironment = environment as? ConfigurableEnvironment ?: return emptyMap()
        val localPortRegex = Regex(pattern = LOCAL_PORT_PATTERN)
        val ports = mutableMapOf<String, String>()

        configurableEnvironment.propertySources
            .filterIsInstance<EnumerablePropertySource<*>>()
            .forEach { propertySource ->
                propertySource.propertyNames.forEach { propertyName ->
                    val serviceName = localPortRegex.find(input = propertyName)?.groupValues?.get(1)
                    val port = propertySource.getProperty(propertyName)
                    if (serviceName != null && port != null && !ports.containsKey(key = serviceName)) {
                        ports[serviceName] = port.toString()
                    }
                }
            }

        return ports
    }

    private fun mergedService(
        name: String,
        discoveredPort: String?,
        configured: BddProperties.Service?
    ): ResolvedService {
        return ResolvedService(
            name = name,
            // ponytail: discovered services inherit the globally configured protocol instead of
            // sniffing "<namespace>.ssl.enabled". Ceiling: an https-only secondary port needs an
            // explicit cucumbertest.services.<name>.protocol. Upgrade path is that one lookup.
            protocol = resolvePlaceholders(value = configured?.protocol)
                ?: bddProperties.server?.protocol
                ?: HTTP_PROTOCOL,
            host = resolvePlaceholders(value = configured?.host)
                ?: bddProperties.server?.host
                ?: DEFAULT_HOST,
            port = resolvePlaceholders(value = configured?.port) ?: discoveredPort,
            basePath = resolvePlaceholders(value = configured?.basePath) ?: defaultBasePathFor(name = name),
            pathPrefixes = configured?.pathPrefixes?.map { prefix -> environment.resolvePlaceholders(prefix) }
                ?: defaultPathPrefixesFor(name = name)
        )
    }

    /**
     * Only the main web server has a base path that feature files never spell out, because the
     * servlet context path is part of its address. Every other service is addressed with the full
     * path, so adding a base path would duplicate it.
     */
    private fun defaultBasePathFor(name: String): String {
        if (name != SERVICE_NAME_SERVER) {
            return ""
        }

        return environment.getProperty(PROPERTY_SERVLET_CONTEXT_PATH)
            ?: environment.getProperty(PROPERTY_WEBFLUX_BASE_PATH)
            ?: ""
    }

    /**
     * The management service claims the actuator path, so `/actuator/...` reaches the management
     * port without any configuration. The main web server gets no prefix on purpose: it is the
     * fallback, and a prefix would divert calls that must keep their current behaviour.
     */
    private fun defaultPathPrefixesFor(name: String): List<String> {
        if (name != SERVICE_NAME_MANAGEMENT) {
            return emptyList()
        }

        val prefix = UrlUtils.appendPathElements(
            path = "",
            environment.getProperty(PROPERTY_MANAGEMENT_BASE_PATH),
            environment.getProperty(PROPERTY_MANAGEMENT_WEB_BASE_PATH) ?: DEFAULT_MANAGEMENT_WEB_BASE_PATH
        )

        return if (prefix.isEmpty()) emptyList() else listOf(prefix)
    }

    /**
     * Picks the service with the longest matching prefix. Ties are broken by name so that the
     * outcome never depends on map iteration order.
     */
    private fun serviceForPath(relativePath: String, services: Map<String, ResolvedService>): ResolvedService? {
        return services.values
            .flatMap { service -> service.pathPrefixes.map { prefix -> service to prefix } }
            .filter { (_, prefix) -> prefix.isNotEmpty() && relativePath.startsWith(prefix = prefix) }
            .sortedWith(
                compareByDescending<Pair<ResolvedService, String>> { (_, prefix) -> prefix.length }
                    .thenBy { (service, _) -> service.name }
            )
            .firstOrNull()
            ?.first
    }

    /**
     * Placeholders are resolved when the request runs, not when the properties are bound, because
     * random ports are only known after the server has started.
     */
    private fun resolvePlaceholders(value: String?): String? {
        if (value == null) {
            return null
        }

        return environment.resolvePlaceholders(value)
    }
}
