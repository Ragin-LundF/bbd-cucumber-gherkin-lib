package com.ragin.bdd.cucumber.security.utils

import io.github.oshai.kotlinlogging.KLogger
import org.testcontainers.containers.output.BaseConsumer
import org.testcontainers.containers.output.OutputFrame

/**
 * Pipes the output of a Testcontainers container into a kotlin-logging logger.
 *
 * Testcontainers ships an SLF4J consumer, but every logger of this library sits under
 * `com.ragin.bdd.cucumber` and is created through `KotlinLogging`, so a project silences the
 * container output with the same `logging.level.com.ragin.bdd.cucumber` entry as everything else.
 */
class KLoggerLogConsumer(private val logger: KLogger) : BaseConsumer<KLoggerLogConsumer>() {
    override fun accept(frame: OutputFrame) {
        val message = frame.utf8StringWithoutLineEnding
        when (frame.type) {
            OutputFrame.OutputType.STDOUT -> logger.info { message }
            OutputFrame.OutputType.STDERR -> logger.error { message }
            else -> {
                // END frame - it carries no container output
            }
        }
    }
}
