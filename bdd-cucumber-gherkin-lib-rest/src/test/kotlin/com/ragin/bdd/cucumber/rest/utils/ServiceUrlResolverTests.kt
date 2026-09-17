package com.ragin.bdd.cucumber.rest.utils

import com.ragin.bdd.cucumber.config.BddProperties
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ServiceUrlResolverTests {

    // --- discovery ---

    @Test
    internal fun `every web server that Spring registered is discovered under its namespace`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "local.management.port" to 9090
            )
        )

        assertEquals(
            expected = setOf("management", "server"),
            actual = resolver.knownServices().keys
        )
    }

    @Test
    internal fun `a discovered service uses localhost and the port that Spring registered`() {
        val resolver = resolverFor(properties = mapOf("local.management.port" to 9090))

        val service = resolver.knownServices().getValue("management")

        assertEquals(expected = "http", actual = service.protocol)
        assertEquals(expected = "localhost", actual = service.host)
        assertEquals(expected = "9090", actual = service.port)
    }

    @Test
    internal fun `no service is discovered when the environment cannot be enumerated`() {
        val resolver = ServiceUrlResolver(
            environment = nonEnumerableEnvironment(),
            bddProperties = bddPropertiesWith(services = emptyMap())
        )

        assertTrue(actual = resolver.knownServices().isEmpty())
    }

    @Test
    internal fun `a discovered service inherits host and protocol from the configured server`() {
        val resolver = ServiceUrlResolver(
            environment = environmentWith(properties = mapOf("local.management.port" to 9090)),
            bddProperties = bddPropertiesWith(
                services = emptyMap(),
                server = BddProperties.Server(protocol = "https", host = "example.com", port = "443")
            )
        )

        val service = resolver.knownServices().getValue("management")

        assertEquals(expected = "https", actual = service.protocol)
        assertEquals(expected = "example.com", actual = service.host)
        assertEquals(expected = "9090", actual = service.port)
    }

    // --- default base paths and prefixes ---

    @Test
    internal fun `the management service claims the actuator path and the server service claims nothing`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "local.management.port" to 9090
            )
        )

        assertEquals(
            expected = listOf("/actuator"),
            actual = resolver.knownServices().getValue("management").pathPrefixes
        )
        assertEquals(
            expected = emptyList(),
            actual = resolver.knownServices().getValue("server").pathPrefixes
        )
    }

    @Test
    internal fun `a custom management base path shifts the claimed prefix`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.management.port" to 9090,
                "management.server.base-path" to "/manage",
                "management.endpoints.web.base-path" to "/ops"
            )
        )

        assertEquals(
            expected = listOf("/manage/ops"),
            actual = resolver.knownServices().getValue("management").pathPrefixes
        )
    }

    @Test
    internal fun `the servlet context path becomes the base path of the server service`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "server.servlet.context-path" to "/ctx"
            )
        )

        assertEquals(expected = "/ctx", actual = resolver.knownServices().getValue("server").basePath)
    }

    @Test
    internal fun `the webflux base path becomes the base path of the server service`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "spring.webflux.base-path" to "/reactive"
            )
        )

        assertEquals(expected = "/reactive", actual = resolver.knownServices().getValue("server").basePath)
    }

    @Test
    internal fun `the management service has no base path so that its prefix is not duplicated`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.management.port" to 9090,
                "management.server.base-path" to "/manage"
            )
        )

        assertEquals(expected = "", actual = resolver.knownServices().getValue("management").basePath)
    }

    // --- routing by path prefix ---

    @Test
    internal fun `a path below the actuator prefix is routed to the management service`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "local.management.port" to 9090
            )
        )

        val service = resolver.resolveFor(relativePath = "/actuator/health", explicitServiceName = null)

        assertEquals(expected = "management", actual = service?.name)
    }

    @Test
    internal fun `an unmatched path is routed to no service at all`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "local.management.port" to 9090
            )
        )

        assertNull(actual = resolver.resolveFor(relativePath = "/api/v1/users", explicitServiceName = null))
    }

    @Test
    internal fun `the longest matching prefix wins when two services claim the same path`() {
        val resolver = resolverFor(
            properties = mapOf("local.server.port" to 8080),
            services = mapOf(
                "broad" to BddProperties.Service(port = "1111", pathPrefixes = listOf("/api")),
                "narrow" to BddProperties.Service(port = "2222", pathPrefixes = listOf("/api/v1"))
            )
        )

        val service = resolver.resolveFor(relativePath = "/api/v1/users", explicitServiceName = null)

        assertEquals(expected = "narrow", actual = service?.name)
    }

    @Test
    internal fun `an empty prefix list disables the automatic routing of a service`() {
        val resolver = resolverFor(
            properties = mapOf("local.management.port" to 9090),
            services = mapOf("management" to BddProperties.Service(pathPrefixes = emptyList()))
        )

        assertNull(actual = resolver.resolveFor(relativePath = "/actuator/health", explicitServiceName = null))
    }

    // --- explicit selection ---

    @Test
    internal fun `an explicitly selected service wins over a matching prefix`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "local.management.port" to 9090
            )
        )

        val service = resolver.resolveFor(relativePath = "/actuator/health", explicitServiceName = "server")

        assertEquals(expected = "server", actual = service?.name)
    }

    @Test
    internal fun `an unknown service name fails and reports the known service names`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "local.management.port" to 9090
            )
        )

        val error = assertFailsWith<IllegalArgumentException> {
            resolver.resolveFor(relativePath = "/api/v1/users", explicitServiceName = "unknown")
        }

        assertTrue(actual = error.message!!.contains("unknown"))
        assertTrue(actual = error.message!!.contains("management, server"))
    }

    @Test
    internal fun `a blank service name is treated as no explicit selection`() {
        val resolver = resolverFor(properties = mapOf("local.management.port" to 9090))

        val service = resolver.resolveFor(relativePath = "/actuator/health", explicitServiceName = " ")

        assertEquals(expected = "management", actual = service?.name)
    }

    // --- configuration ---

    @Test
    internal fun `configuration overrides a discovered service field by field`() {
        val resolver = resolverFor(
            properties = mapOf("local.management.port" to 9090),
            services = mapOf(
                "management" to BddProperties.Service(protocol = "https", basePath = "/admin")
            )
        )

        val service = resolver.knownServices().getValue("management")

        assertEquals(expected = "https", actual = service.protocol)
        assertEquals(expected = "/admin", actual = service.basePath)
        assertEquals(expected = "localhost", actual = service.host)
        assertEquals(expected = "9090", actual = service.port)
    }

    @Test
    internal fun `a service that exists only in the configuration is addressable`() {
        val resolver = resolverFor(
            properties = mapOf("local.server.port" to 8080),
            services = mapOf(
                "payment-mock" to BddProperties.Service(host = "127.0.0.1", port = "7070")
            )
        )

        val service = resolver.resolveFor(relativePath = "/v1/payments", explicitServiceName = "payment-mock")

        assertEquals(expected = "127.0.0.1", actual = service?.host)
        assertEquals(expected = "7070", actual = service?.port)
    }

    @Test
    internal fun `a configured port placeholder is resolved when the request runs`() {
        val resolver = resolverFor(
            properties = mapOf("local.management.port" to 9090),
            services = mapOf("admin" to BddProperties.Service(port = "\${local.management.port}"))
        )

        assertEquals(expected = "9090", actual = resolver.knownServices().getValue("admin").port)
    }

    @Test
    internal fun `a configured host placeholder is resolved when the request runs`() {
        val resolver = resolverFor(
            properties = mapOf(
                "local.server.port" to 8080,
                "mock.host" to "mock.internal"
            ),
            services = mapOf("mock" to BddProperties.Service(host = "\${mock.host}", port = "7070"))
        )

        assertEquals(expected = "mock.internal", actual = resolver.knownServices().getValue("mock").host)
    }

    @Test
    internal fun `a placeholder that cannot be resolved fails when that service is used`() {
        val resolver = resolverFor(
            properties = mapOf("local.server.port" to 8080),
            services = mapOf("admin" to BddProperties.Service(port = "\${local.management.port}"))
        )

        val error = assertFailsWith<IllegalArgumentException> {
            resolver.resolveFor(relativePath = "/whatever", explicitServiceName = "admin")
        }

        assertTrue(actual = error.message!!.contains("admin"))
        assertTrue(actual = error.message!!.contains("local.management.port"))
    }

    @Test
    internal fun `a placeholder that cannot be resolved does not break another service`() {
        val resolver = resolverFor(
            properties = mapOf("local.management.port" to 9090),
            services = mapOf("admin" to BddProperties.Service(port = "\${missing.port}"))
        )

        val service = resolver.resolveFor(relativePath = "/actuator/health", explicitServiceName = null)

        assertEquals(expected = "management", actual = service?.name)
    }

    // --- fixtures ---

    private fun resolverFor(
        properties: Map<String, Any>,
        services: Map<String, BddProperties.Service> = emptyMap()
    ): ServiceUrlResolver {
        return ServiceUrlResolver(
            environment = environmentWith(properties = properties),
            bddProperties = bddPropertiesWith(services = services)
        )
    }

    private fun environmentWith(properties: Map<String, Any>): Environment {
        val environment = StandardEnvironment()
        environment.propertySources.addFirst(
            MapPropertySource("test-properties", properties)
        )

        return environment
    }

    private fun bddPropertiesWith(
        services: Map<String, BddProperties.Service>,
        server: BddProperties.Server? = null
    ): BddProperties {
        return BddProperties(
            authorization = null,
            proxy = null,
            server = server,
            ssl = null,
            services = services
        )
    }

    /**
     * An environment that is not a ConfigurableEnvironment, so nothing can be enumerated from it.
     *
     * Built as a proxy because the architecture tests allow no additional class in a test file, and
     * every Spring implementation of Environment is configurable. The resolver bails out before it
     * calls anything on it, so the handler is never asked for a value.
     */
    private fun nonEnumerableEnvironment(): Environment {
        return Proxy.newProxyInstance(
            Environment::class.java.classLoader,
            arrayOf(Environment::class.java)
        ) { _, method, _ ->
            throw UnsupportedOperationException("Unexpected call to ${method.name}")
        } as Environment
    }
}
