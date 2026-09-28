package com.ragin.bdd.cucumber.rest.extensions

import io.cucumber.datatable.DataTable
import io.cucumber.datatable.DataTableTypeRegistry
import io.cucumber.datatable.DataTableTypeRegistryTableConverter
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class DataTableExtensionTests {

    @Test
    internal fun `asNonNullMap returns all rows in table order`() {
        val dataTable = dataTableOf(listOf(listOf("b", "2"), listOf("a", "1")))

        val result = dataTable.asNonNullMap()

        assertEquals(expected = listOf("b" to "2", "a" to "1"), actual = result.toList())
    }

    @Test
    internal fun `asNonNullMap fails with the key when a value cell is empty`() {
        val dataTable = dataTableOf(listOf(listOf("resourceId", null)))

        val exception = assertFailsWith<IllegalArgumentException> { dataTable.asNonNullMap() }

        assertEquals(expected = "DataTable contains an empty value for key 'resourceId'", actual = exception.message)
    }

    @Test
    internal fun `asNonNullMap fails with the value when a key cell is empty`() {
        val dataTable = dataTableOf(listOf(listOf("resourceId", "abc-def"), listOf(null, "abc")))

        val exception = assertFailsWith<IllegalArgumentException> { dataTable.asNonNullMap() }

        assertEquals(expected = "DataTable contains an empty key for value 'abc'", actual = exception.message)
    }

    @Test
    internal fun `asMultiValueMap collects repeated field names into one entry`() {
        val dataTable = dataTableOf(listOf(listOf("file", "a.txt"), listOf("file", "b.txt"), listOf("id", "1")))

        val result = dataTable.asMultiValueMap()

        assertEquals(expected = mapOf("file" to listOf("a.txt", "b.txt"), "id" to listOf("1")), actual = result.toMap())
    }

    @Test
    internal fun `asMultiValueMap sends an empty value cell as empty form field`() {
        val dataTable = dataTableOf(listOf(listOf("comment", null)))

        val result = dataTable.asMultiValueMap()

        assertEquals(expected = mapOf("comment" to listOf("")), actual = result.toMap())
    }

    @Test
    internal fun `asMultiValueMap fails with the value when a field name cell is empty`() {
        val dataTable = dataTableOf(listOf(listOf(null, "abc")))

        val exception = assertFailsWith<IllegalArgumentException> { dataTable.asMultiValueMap() }

        assertEquals(expected = "DataTable contains an empty field name for value 'abc'", actual = exception.message)
    }

    /**
     * Uses the converter Cucumber uses at runtime. An empty cell of a feature file reaches the table as `null`.
     */
    private fun dataTableOf(rows: List<List<String?>>): DataTable {
        return DataTable.create(rows, DataTableTypeRegistryTableConverter(DataTableTypeRegistry(Locale.ENGLISH)))
    }
}
