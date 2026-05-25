package com.ragin.bdd.cucumbertests.extensions

import com.ragin.bdd.cucumber.utils.BddJacksonUtils

fun Map<String, Any?>.toJsonString(): String{
    return BddJacksonUtils.mapper.writeValueAsString(this)
}
