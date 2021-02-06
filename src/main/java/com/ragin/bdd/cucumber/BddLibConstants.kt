package com.ragin.bdd.cucumber

import java.util.regex.Pattern

object BddLibConstants {
    const val BDD_LIB_NOT = "@bdd_lib_not "
    const val BDD_LIB_NOT_EXISTS = "@bdd_lib_not_exist"
    val BDD_LIB_MATCHER_PATTERN: Pattern = Pattern.compile("(\\$\\{json-unit\\.matches:)(\\w*)(.*)")
}