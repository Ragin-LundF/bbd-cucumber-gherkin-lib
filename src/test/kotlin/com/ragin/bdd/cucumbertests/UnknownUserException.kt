package com.ragin.bdd.cucumbertests

import java.io.Serial

class UnknownUserException(message: String?) : RuntimeException(message) {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 6028232741646374784L
    }
}
