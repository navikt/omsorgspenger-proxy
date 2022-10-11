package no.nav.omsorgspenger

import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpDelete
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpOptions
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpPatch
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpPost
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpPut
import org.slf4j.LoggerFactory

internal object KtorHttp {

    private val config = SimpleHttpClient.Config(engine = Java)
    private val logger = LoggerFactory.getLogger(KtorHttp::class.java)

    internal suspend fun ApplicationCall.forwardPatch(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        kotlin.runCatching { receiveNullable<ByteArray>() }.getOrNull().also { body ->
            forward {
                toUrl.httpPatch(config) { builder ->
                    populateBuilder(
                        builder = builder,
                        extraHeaders = extraHeaders,
                        body = body
                    )
                }
            }
        }
    }

    internal suspend fun ApplicationCall.forwardPost(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        kotlin.runCatching { receiveNullable<ByteArray>() }.getOrNull().also { body ->
            forward {
                toUrl.httpPost(config) { builder ->
                    populateBuilder(
                        builder = builder,
                        extraHeaders = extraHeaders,
                        body = body
                    )
                }
            }
        }
    }

    internal suspend fun ApplicationCall.forwardPut(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        kotlin.runCatching { receiveNullable<ByteArray>() }.getOrNull().also { body ->
            forward {
                toUrl.httpPut(config) { builder ->
                    populateBuilder(
                        builder = builder,
                        extraHeaders = extraHeaders,
                        body = body
                    )
                }
            }
        }
    }

    internal suspend fun ApplicationCall.forwardDelete(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        kotlin.runCatching { receiveNullable<ByteArray>() }.getOrNull().also { body ->
            forward {
                toUrl.httpDelete(config) { builder ->
                    populateBuilder(
                        builder = builder,
                        extraHeaders = extraHeaders,
                        body = body
                    )
                }
            }
        }
    }

    internal suspend fun ApplicationCall.forwardOptions(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        forward {
            toUrl.httpOptions(config) { builder ->
                populateBuilder(
                    builder = builder,
                    extraHeaders = extraHeaders,
                    body = null
                )
            }
        }
    }

    internal suspend fun ApplicationCall.doGet(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) =
        toUrl.httpGet(config) { builder ->
            populateBuilder(
                builder = builder,
                extraHeaders = extraHeaders,
                body = null
            )
        }

    internal suspend fun ApplicationCall.forwardGet(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        forward { doGet(toUrl, extraHeaders) }
    }

    internal suspend fun ApplicationCall.forward(
        respondOnError: Boolean = true,
        block: suspend () -> Pair<HttpRequestData, Result<HttpResponse>>
    ): Boolean {
        val (httpRequestData, httpResponseResult) = block()

        return httpResponseResult.fold(
            onSuccess = {
                val doRespond = when (respondOnError) {
                    true -> true
                    false -> it.status.isSuccess()
                }

                val responseBody = it.body<ByteArray>()
                if (!doRespond || it.status.value >= 500) {
                    val queryNames = httpRequestData.url.parameters.names()
                    val urlUtenQueryParameters = "${httpRequestData.url}".substringBefore("?")
                    logger.error(
                        "Uventet response gjennom proxy: Method=[${httpRequestData.method.value}], Url=[$urlUtenQueryParameters], QueryNames=$queryNames, Accept=[${httpRequestData.headers[HttpHeaders.Accept]}], HttpStatusCode=[${it.status.value}], Response=[${
                        String(
                            responseBody
                        )
                        }]"
                    )
                }

                if (doRespond) {
                    it.headers.forEach { key, values ->
                        if (!HttpHeaders.isUnsafe(key)) {
                            values.forEach { value ->
                                response.header(key, value)
                            }
                        }
                    }
                    respondBytes(
                        contentType = it.contentType(),
                        status = it.status,
                        bytes = responseBody
                    )
                }

                doRespond
            },
            onFailure = {
                logger.error(
                    "Feil ved proxy av request: Method=[${httpRequestData.method.value}], Url=[${httpRequestData.url}]",
                    it
                )
                if (respondOnError) {
                    respondText(
                        status = HttpStatusCode.BadGateway,
                        text = "Unable to proxy request."
                    )
                }
                respondOnError
            }
        )
    }

    @OptIn(InternalAPI::class)
    private fun ApplicationCall.populateBuilder(
        builder: HttpRequestBuilder,
        body: ByteArray?,
        extraHeaders: Map<String, Any?>
    ) {
        // Body
        body?.also {
            builder.body = ByteArrayContent(
                bytes = it,
                contentType = request.contentType()
            )
        }

        // Timeout
        builder.timeout {
            connectTimeoutMillis = 20_000
            requestTimeoutMillis = 20_000
            socketTimeoutMillis = 20_000
        }

        // Headers
        val extra = extraHeaders.filterValues { it != null }.mapValues { it.value!! }
        val remove = extraHeaders.filterValues { it == null }
        request.headers.forEach { key, values ->
            if (key !in remove && !HttpHeaders.isUnsafe(key)) {
                values.forEach { value -> builder.header(key, value) }
            }
        }
        extra.forEach { (key, value) ->
            builder.header(key, value)
        }
    }
}
