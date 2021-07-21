package com.ragin.bdd.cucumber.glue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.BaseCucumberCore
import com.ragin.bdd.cucumber.core.IDatabaseExecutorService
import com.ragin.bdd.cucumber.utils.JsonUtils
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.apache.commons.lang3.StringUtils
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.util.stream.Collectors

/**
 * This class contains database related steps.
 */
open class DatabaseGlue(
    jsonUtils: JsonUtils,
    bddProperties: BddProperties,
    private val databaseExecutorService: IDatabaseExecutorService
) : BaseCucumberCore(jsonUtils, bddProperties) {
    private val mapper = ObjectMapper()

    /**
     * Initialize the database with a file.
     *
     * @param pathToFile    path/filename of the file, which contains a liquibase script
     * @throws Exception    Error with executing database statements
     */
    @Given("that the database was initialized with the liquibase file {string}")
    @Transactional
    @Throws(Exception::class)
    open fun givenThatTheDatabaseWasInitializedWithLiquibaseFile(pathToFile: String) {
        databaseExecutorService.executeLiquibaseScript(getFilePath(pathToFile))
    }

    /**
     * Execute a file which contains SQL statements
     *
     * @param pathToQueryFile       Path/filename to file with query (SQL)
     * @throws java.io.IOException  Unable to read file or to parse results
     */
    @Given("that the SQL statements from the SQL file {string} was executed")
    @Transactional
    @Throws(IOException::class)
    open fun givenThatTheDatabaseWasInitializedWithSQLFile(pathToQueryFile: String) {
        // read file
        val sqlStatements = readFile(pathToQueryFile)
        // execute query
        databaseExecutorService.executeSQL(sqlStatements)
    }

    /**
     * Ensure that the result of a query of a file is equal to a result list in a CSV file.
     *
     * @param pathToQueryFile   Path/filename to file with query (SQL)
     * @param pathToCsvFile     Path/filename to CSV file with result to compare
     * @throws IOException      Unable to read file or to parse results
     */
    @Then("I ensure that the result of the query of the file {string} is equal to the CSV file {string}")
    @Transactional
    @Throws(IOException::class)
    open fun thenEnsureThatResultOfQueryOfFileIsEqualToCSV(pathToQueryFile: String, pathToCsvFile: String) {
        // read file
        val sqlStatements = readFile(pathToQueryFile)
        // execute query
        val queryResults = generifyDatabaseJSONFiles(databaseExecutorService.executeQuerySQL(sqlStatements))

        // Generify the results to be database independent
        val iterator: Iterator<Map<String, Any?>> = CsvMapper()
            .readerFor(MutableMap::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues(readFile(pathToCsvFile))

        // write data into list
        val csvList: MutableList<Map<String, Any?>> = ArrayList()
        while (iterator.hasNext()) {
            csvList.add(iterator.next())
        }
        val data = generifyDatabaseJSONFiles(csvList)

        // Convert results to JSON to reuse compare
        val expectedResultAsJSON = mapper.writeValueAsString(data)
        val actualResultAsJSON = mapper.writeValueAsString(queryResults)

        // compare JSON
        assertJSONisEqual(expectedResultAsJSON, actualResultAsJSON)
    }

    /**
     * Generify the given JSON in order to be able to check it across several databases.
     *
     * This is required since not all databases return the column name in a common format, nor the saved value.
     *
     * @see DatabaseGlue.generifyDatabaseColumnName
     * @see DatabaseGlue.generifyDatabaseColumnValue
     * @param data List of database rows as Map with column name and columns value
     * @return the generified data to be database independent
     */
    private fun generifyDatabaseJSONFiles(data: List<Map<String, Any?>>): List<Map<String, Any>> {
        return data.stream().map { original: Map<String, Any?> ->
            original
                .entries
                .stream()
                .filter { entry: Map.Entry<String, Any?> -> entry.value != null }
                .filter { entry: Map.Entry<String, Any?> -> entry.value !is String || StringUtils.isNotEmpty(entry.value as String?) }
                .collect(Collectors.toMap(
                    { columnName: Map.Entry<String, Any?> -> generifyDatabaseColumnName(columnName.key) },
                    { columnValue: Map.Entry<String, Any?> -> generifyDatabaseColumnValue(columnValue.value) }
                ))
        }.collect(Collectors.toList())
    }

    /**
     * Generify the given `columnName` for database independent compare.
     *
     * It applies the following modifications:
     *
     *  * Makes it "uppercase", so that the comparison is case insensitive.
     *
     *
     * @param columnName the columnName to be processed
     * @return the columnName processed to be database independent
     */
    private fun generifyDatabaseColumnName(columnName: String): String {
        return columnName.uppercase()
    }

    /**
     * Generify the given `columnValue` for database independent compare.
     *
     * It applies the following modifications:
     *
     *  * Boolean values are mapped to either "0" or "1".
     *  * "true" and "false" strings are mapped to either "0" or "1".
     *
     *
     * @param columnValue the columnValue to be processed
     * @param <V> any object
     * @return the columnName processed to be database independent
    </V> */
    private fun <V> generifyDatabaseColumnValue(columnValue: V): Any {
        if (columnValue is Boolean) {
            return if (columnValue) 1 else 0
        }
        if (columnValue is String) {
            if ("true".equals(columnValue as String, ignoreCase = true)) {
                return 1
            } else if ("false".equals(columnValue as String, ignoreCase = true)) {
                return 0
            }
        }
        return columnValue.toString()
    }
}