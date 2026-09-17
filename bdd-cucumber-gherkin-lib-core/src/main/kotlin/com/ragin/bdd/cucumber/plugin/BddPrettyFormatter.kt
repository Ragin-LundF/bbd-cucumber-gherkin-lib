package com.ragin.bdd.cucumber.plugin

import io.cucumber.messages.types.Envelope
import io.cucumber.plugin.ColorAware
import io.cucumber.plugin.ConcurrentEventListener
import io.cucumber.plugin.event.EventPublisher
import io.cucumber.prettyformatter.MessagesToPrettyWriter
import io.cucumber.prettyformatter.MessagesToPrettyWriter.PrettyFeature.INCLUDE_ATTACHMENTS
import io.cucumber.prettyformatter.MessagesToPrettyWriter.PrettyFeature.INCLUDE_FEATURE_LINE
import io.cucumber.prettyformatter.MessagesToPrettyWriter.PrettyFeature.INCLUDE_RULE_LINE
import io.cucumber.prettyformatter.Theme
import java.io.File
import java.io.OutputStream

/**
 * Prints the scenarios and steps while they run, without echoing the attached payloads.
 *
 * Cucumber's own `pretty` plugin includes attachments, which would dump every request and response
 * body onto the console. This library reports the short summary lines through the logging framework
 * and attaches the payloads to the report, so the console only needs the structure around them.
 *
 * Register it with the constant in `BddLibConfigConstants.Plugin`, appended to the plugins the
 * project already configures.
 */
class BddPrettyFormatter(out: OutputStream) : ConcurrentEventListener, ColorAware {
    private var writer: MessagesToPrettyWriter = createWriter(out = out, theme = Theme.cucumber())
    private val out: OutputStream = out

    override fun setEventPublisher(publisher: EventPublisher) {
        publisher.registerHandlerFor(Envelope::class.java) { envelope -> write(envelope = envelope) }
    }

    override fun setMonochrome(monochrome: Boolean) {
        if (monochrome) {
            writer = createWriter(out = out, theme = Theme.plain())
        }
    }

    private fun write(envelope: Envelope) {
        writer.write(envelope)

        if (envelope.testRunFinished.isPresent) {
            writer.close()
        }
    }

    private fun createWriter(out: OutputStream, theme: Theme): MessagesToPrettyWriter {
        return MessagesToPrettyWriter.builder()
            .feature(INCLUDE_FEATURE_LINE, false)
            .feature(INCLUDE_RULE_LINE, false)
            // The payloads live in the report as collapsed blocks; echoing them here would bury the
            // steps they belong to.
            .feature(INCLUDE_ATTACHMENTS, false)
            .theme(theme)
            .removeUriPrefix(File("").toPath().toUri().toString())
            .build(out)
    }
}
