package configuration.com.ragin.bdd.cucumber.glue

import com.ragin.bdd.cucumber.core.DatabaseExecutorService
import com.ragin.bdd.cucumber.core.IDatabaseExecutorService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
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
                throw IllegalArgumentException("""Missing bean: ${DataSource::class.qualifiedName}""")
            }
            jdbcTemplate.isEmpty -> {
                throw IllegalArgumentException("""Missing bean: ${JdbcTemplate::class.qualifiedName}""")
            }
            else -> {
                return DatabaseExecutorService(dataSource.get(), jdbcTemplate.get())
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
            @Throws(Exception::class)
            override fun executeLiquibaseScript(liquibaseScript: String) = Unit

            override fun executeSQL(sql: String) = Unit
            override fun executeQuerySQL(sql: String): List<Map<String, Any>> {
                return Collections.emptyList()
            }
        }
    }
}
