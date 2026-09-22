package configuration.com.ragin.bdd.cucumber.security

import com.ragin.bdd.cucumber.security.SecurityScanSession
import com.ragin.bdd.cucumber.security.SecurityScanner
import com.ragin.bdd.cucumber.security.config.SecurityScanProperties
import com.ragin.bdd.cucumber.security.zap.ZapSecurityScanner
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

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
 * **Swapping the scanner**: define your own [SecurityScanner] bean. The ZAP bean here is
 * conditional on no [SecurityScanner] being present, so yours wins and ZAP is never started.
 * Nothing else in the project changes.
 */
@AutoConfiguration
class SecurityScanBeanConfig {
    /**
     * Binds `cucumbertest.security` by hand instead of through `@EnableConfigurationProperties`.
     *
     * [SecurityScanProperties] lives in the security core module, which carries no Spring
     * dependency so that projects on another Spring Boot generation can use it. [Binder] gives the
     * same relaxed, constructor based binding the annotation would.
     */
    @Bean
    @ConditionalOnMissingBean(SecurityScanProperties::class)
    fun securityScanProperties(environment: Environment): SecurityScanProperties {
        return Binder.get(environment)
            .bind(SecurityScanProperties.PREFIX, SecurityScanProperties::class.java)
            .orElseGet { SecurityScanProperties() }
    }

    @Bean
    @ConditionalOnMissingBean(SecurityScanner::class)
    fun securityScanner(properties: SecurityScanProperties): SecurityScanner {
        return ZapSecurityScanner.create(properties = properties)
    }

    /**
     * One session for the whole run, so the hook that remembers the targets and the step that
     * attacks them work on the same state. It is the same entry point a plain jUnit suite uses.
     */
    @Bean
    fun securityScanSession(
        properties: SecurityScanProperties,
        scanner: SecurityScanner
    ): SecurityScanSession {
        return SecurityScanSession(properties = properties, scanner = scanner)
    }
}
