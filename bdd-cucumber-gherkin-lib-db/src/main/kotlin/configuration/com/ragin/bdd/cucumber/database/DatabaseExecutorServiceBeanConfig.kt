package configuration.com.ragin.bdd.cucumber.database

import com.ragin.bdd.cucumber.database.executor.DatabaseExecutorService
import com.ragin.bdd.cucumber.database.executor.IDatabaseExecutorService
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.sql.DataSource
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import java.util.Optional

class DatabaseExecutorServiceBeanConfig(
    private val dataSource: Optional<DataSource>,
    private val jdbcTemplate: Optional<JdbcTemplate>
) {
    @Bean
    @ConditionalOnProperty(
        prefix = "cucumberTest",
        name = ["databaseless"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun databaseExecutorService(): IDatabaseExecutorService {
        when {
            dataSource.isEmpty -> {
                throw IllegalArgumentException("Missing bean: ${DataSource::class.qualifiedName}")
            }

            jdbcTemplate.isEmpty -> {
                throw IllegalArgumentException("Missing bean: ${JdbcTemplate::class.qualifiedName}")
            }

            else -> {
                return DatabaseExecutorService(
                    datasource = dataSource.get(),
                    jdbcTemplate = jdbcTemplate.get()
                )
            }
        }
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "cucumberTest",
        name = ["databaseless"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun databaseExec(): IDatabaseExecutorService {
        return object : IDatabaseExecutorService {
            @Throws(exceptionClasses = [Exception::class])
            override fun executeLiquibaseScript(liquibaseScript: String) {
                warnDiscarded(what = "Liquibase script $liquibaseScript")
            }

            override fun executeSQL(sql: String) {
                warnDiscarded(what = "SQL statement")
            }

            override fun executeQuerySQL(sql: String): List<Map<String, Any>> {
                warnDiscarded(what = "SQL query")
                return emptyList()
            }

            /**
             * Without this, a project running databaseless debugs an insert that never happened.
             */
            private fun warnDiscarded(what: String) {
                log.warn { "cucumberTest.databaseless is enabled, $what was not executed" }
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
