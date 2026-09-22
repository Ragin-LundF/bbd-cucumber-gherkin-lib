package configuration.com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.SecurityScan
import com.ragin.bdd.cucumber.security.SecurityScanner
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.zap.ZapApiClient
import com.ragin.bdd.cucumber.security.zap.ZapContainer
import com.ragin.bdd.cucumber.security.zap.ZapSecurityScanner
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Registers the security scan without the consuming project having to touch its
 * `@CucumberContextConfiguration` class.
 *
 * Only the collaborators are declared here. The Cucumber glue and hooks are registered - and
 * constructor-injected - by cucumber-spring itself; declaring them as beans as well would make
 * them ambiguous.
 *
 * The beans are always created, but every step and hook is a no-op while
 * `cucumbertest.security.enabled` is false, which is the default. That way the module can stay
 * on the test classpath of the regular cucumber run.
 *
 * **Swapping the scanner**: define your own [SecurityScanner] bean. Every ZAP bean here is
 * conditional on no [SecurityScanner] being present, so yours wins and the ZAP ones are not
 * created at all. Nothing else in the project changes.
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityScanProperties::class)
class SecurityScanBeanConfig {
    @Bean
    @ConditionalOnMissingBean(SecurityScanner::class)
    fun zapContainer(properties: SecurityScanProperties): ZapContainer {
        return ZapContainer(properties = properties)
    }

    @Bean
    @ConditionalOnMissingBean(SecurityScanner::class)
    fun zapApiClient(container: ZapContainer): ZapApiClient {
        return ZapApiClient(container = container)
    }

    @Bean
    @ConditionalOnMissingBean(SecurityScanner::class)
    fun securityScanner(
        properties: SecurityScanProperties,
        container: ZapContainer,
        client: ZapApiClient
    ): SecurityScanner {
        return ZapSecurityScanner(properties = properties, container = container, client = client)
    }

    @Bean
    fun securityScan(properties: SecurityScanProperties, scanner: SecurityScanner): SecurityScan {
        return SecurityScan(properties = properties, scanner = scanner)
    }
}
