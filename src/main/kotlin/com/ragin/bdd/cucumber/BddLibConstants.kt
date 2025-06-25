package com.ragin.bdd.cucumber

import java.util.regex.Pattern.UNICODE_CHARACTER_CLASS

object BddLibConstants {
    const val BDD_LIB_NOT = "@bdd_lib_not "
    const val BDD_LIB_NOT_EXISTS = "@bdd_lib_not_exist"
    val BDD_LIB_MATCHER_PATTERN = "(\\$\\{json-unit\\.matches:)(\\w*)(.*)".toPattern(
        flags = UNICODE_CHARACTER_CLASS
    )

    const val BDD_URI_ELEMENTS = "URI Elements"
    const val BDD_URI_VALUES = "URI Values"
}
