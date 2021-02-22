package com.ragin.bdd.cucumber.core

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

class DatabaseExecutorService(private val datasource: DataSource, private val jdbcTemplate: JdbcTemplate) :
    IDatabaseExecutorService {
    @Value("\${cucumberTest.liquibase.closeConnection:false}")
    val closeConnection = false

    /**
     * Execute Liquibase script
     *
     * @param liquibaseScript   path/filename of the Liquibase script
     * @throws Exception        Unable to execute liquibase script
     */
    @Throws(Exception::class)
    override fun executeLiquibaseScript(liquibaseScript: String) {
        val connection = JdbcConnection(datasource.connection)
        try {
            Liquibase(
                    liquibaseScript,
                    ClassLoaderResourceAccessor(),
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
            ).use { liquibase ->
                liquibase.update(
                        Contexts(""),
                        LabelExpression(),
                        false
                )
            }
        } finally {
            if (closeConnection) {
                connection.close()
            }
        }
    }

    /**
     * Execute SQL statements
     *
     * @param sql   SQL statements that should be executed
     */
    override fun executeSQL(sql: String) {
        jdbcTemplate.execute(sql)
    }

    /**
     * Execute an query of SQL statements
     *
     * @param sql   SQL statements that should be executed
     * @return      List with a map per row which contains the result
     */
    override fun executeQuerySQL(sql: String): List<Map<String, Any>> {
        return jdbcTemplate.queryForList(sql)
    }
}