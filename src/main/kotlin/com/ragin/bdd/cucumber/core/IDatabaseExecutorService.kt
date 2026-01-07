package com.ragin.bdd.cucumber.core

/**
 * Interface for the DatabaseExecutorService
 */
interface IDatabaseExecutorService {
    /**
     * Execute Liquibase script
     *
     * @param liquibaseScript   path/filename of the Liquibase script
     * @throws Exception        Unable to execute liquibase script
     */
    @Throws(Exception::class)
    fun executeLiquibaseScript(liquibaseScript: String)

    /**
     * Execute SQL statements
     *
     * @param sql   SQL statements that should be executed
     */
    fun executeSQL(sql: String)

    /**
     * Execute an query of SQL statements
     *
     * @param sql   SQL statements that should be executed
     * @return      List with a map per row which contains the result
     */
    fun executeQuerySQL(sql: String): List<Map<String, Any?>>
}
