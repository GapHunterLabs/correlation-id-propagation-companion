package dev.gaphunter.correlationidpropagationcompanion.detect

/**
 * Text signals this plugin uses to spot correlation/trace-id handling --
 * deliberately broad substring matches (not resolved calls), same
 * "match a known name, don't resolve a symbol" discipline as
 * `SqlSignalNames`/`SignatureVerificationSignals` elsewhere in this
 * catalog.
 */
object CorrelationSignals {

    /** Header/param-name fragments that mark a `@RequestHeader` as carrying a correlation/trace id. */
    private val HEADER_NAME_FRAGMENTS = listOf(
        "correlation",
        "trace-id",
        "traceid",
        "trace_id",
        "request-id",
        "requestid",
        "request_id",
        "x-b3-traceid",
    )

    /** Signals that a method body makes an outbound HTTP call -- broad, framework-agnostic text match. */
    private val OUTBOUND_HTTP_FRAGMENTS = listOf(
        "resttemplate",
        "webclient",
        "okhttpclient",
        "httpclient",
        "feignclient",
        ".exchange(",
        ".getforobject(",
        ".getforentity(",
        ".postforobject(",
        ".postforentity(",
        ".execute(",
        "httprequest.newbuilder",
    )

    fun headerNameLooksLikeCorrelationId(name: String): Boolean {
        val lower = name.lowercase()
        return HEADER_NAME_FRAGMENTS.any { lower.contains(it) }
    }

    fun bodyMakesOutboundHttpCall(bodyText: String): Boolean {
        val lower = bodyText.lowercase()
        return OUTBOUND_HTTP_FRAGMENTS.any { lower.contains(it) }
    }
}
