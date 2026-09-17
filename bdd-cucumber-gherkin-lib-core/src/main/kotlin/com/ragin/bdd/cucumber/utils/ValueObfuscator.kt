package com.ragin.bdd.cucumber.utils

/**
 * Hides the middle of a sensitive value while keeping both ends readable.
 *
 * The first and the last third of the characters stay visible and the middle third is replaced with
 * `*`, so a token can still be recognised and compared without being usable. The split uses integer
 * division, which means the masked part absorbs the remainder and is never shorter than a third:
 *
 * ```
 * "abcdefghi"                -> "abc***ghi"
 * "abcdefghij"               -> "abc****hij"
 * "ab"                       -> "**"
 * ""                         -> ""
 * ```
 */
object ValueObfuscator {

    /**
     * Obfuscates the middle third of the given value.
     *
     * @param value value to obfuscate
     * @return the value with its middle third replaced by `*`
     */
    fun obfuscate(value: String): String {
        val visibleLength = value.length / VISIBLE_PARTS
        val maskedLength = value.length - (visibleLength * 2)

        if (maskedLength <= 0) {
            return value
        }

        return value.take(n = visibleLength) +
            MASK_CHARACTER.toString().repeat(n = maskedLength) +
            value.takeLast(n = visibleLength)
    }

    /**
     * Obfuscates every credential that appears inside a longer text.
     *
     * Needed because a payload is reported as it came off the wire, and an API that echoes the
     * request, or an error response that quotes it, carries the credential with it. The replacement
     * keeps the original length, so a JSON payload stays valid JSON.
     *
     * @param text text that may contain credentials
     * @return the text with every recognised credential obfuscated
     */
    fun obfuscateSecretsIn(text: String): String {
        return CREDENTIAL_PATTERN.replace(input = text) { match ->
            match.groupValues[1] + obfuscate(value = match.groupValues[2])
        }
    }

    private const val VISIBLE_PARTS = 3
    private const val MASK_CHARACTER = '*'

    /**
     * Matches an HTTP authentication scheme followed by its credential.
     *
     * The credential character class deliberately excludes quotes and whitespace so that a match
     * stops at the end of a JSON string value or a header line.
     */
    private val CREDENTIAL_PATTERN = Regex(
        pattern = "((?:Bearer|Basic|Digest)\\s+)([A-Za-z0-9._~+/=-]{8,})",
        option = RegexOption.IGNORE_CASE
    )
}
