package configuration.com.ragin.bdd.cucumber.database

import com.ragin.bdd.cucumber.database.executor.DatabaseExecutorService
import com.ragin.bdd.cucumber.database.executor.IDatabaseExecutorService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import java.util.Optional
import javax.sql.DataSource

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
            override fun executeLiquibaseScript(liquibaseScript: String) = Unit

            override fun executeSQL(sql: String) = Unit
            override fun executeQuerySQL(sql: String): List<Map<String, Any>> {
                return emptyList()
            }
        }
    }
}
