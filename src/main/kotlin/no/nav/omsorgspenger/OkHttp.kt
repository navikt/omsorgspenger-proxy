package no.nav.omsorgspenger

import io.ktor.application.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpDelete
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpOptions
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpPatch
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpPost
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpPut
import org.slf4j.LoggerFactory

internal object OkHttp {

    private val logger = LoggerFactory.getLogger(OkHttp::class.java)

    internal suspend fun ApplicationCall.forwardPatch(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        receiveOrNull<ByteArray>().also { body -> forward {
            toUrl.httpPatch { builder ->
                populateBuilder(
                    builder = builder,
                    extraHeaders = extraHeaders,
                    body = body
                )
            }
        }}
    }
    internal suspend fun ApplicationCall.forwardPost(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        receiveOrNull<ByteArray>().also { body -> forward {
            toUrl.httpPost { builder ->
                populateBuilder(
                    builder = builder,
                    extraHeaders = extraHeaders,
                    body = body
                )
            }
        }}
    }
    internal suspend fun ApplicationCall.forwardPut(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        receiveOrNull<ByteArray>().also { body -> forward {
            toUrl.httpPut { builder ->
                populateBuilder(
                    builder = builder,
                    extraHeaders = extraHeaders,
                    body = body
                )
            }
        }}
    }
    internal suspend fun ApplicationCall.forwardDelete(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        receiveOrNull<ByteArray>().also { body -> forward {
            toUrl.httpDelete { builder ->
                populateBuilder(
                    builder = builder,
                    extraHeaders = extraHeaders,
                    body = body
                )
            }
        }}
    }
    internal suspend fun ApplicationCall.forwardOptions(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        forward {
            toUrl.httpOptions { builder ->
                populateBuilder(
                    builder = builder,
                    extraHeaders = extraHeaders,
                    body = null
                )
            }
        }
    }
    internal suspend fun ApplicationCall.forwardGet(toUrl: String, extraHeaders: Map<String, Any?> = emptyMap()) {
        forward {
            toUrl.httpGet { builder ->
                populateBuilder(
                    builder = builder,
                    extraHeaders = extraHeaders,
                    body = null
                )
            }
        }
    }

    private suspend fun ApplicationCall.forward(block: suspend () -> Pair<HttpRequestData, Result<HttpResponse>>) {
        val (httpRequestData, httpResponseResult) = block()

        httpResponseResult.fold(
            onSuccess = {
                val responseBody = it.receive<ByteArray>()
                if (it.status.value >= 500) {
                    logger.error("Uventet response gjennom proxy: Method=[${httpRequestData.method.value}], Url=[${httpRequestData.url}], HttpStatusCode=[${it.status.value}], Response=[${String(responseBody)}]")
                }

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
            },
            onFailure = {
                logger.error("Feil ved proxy av request: Method=[${httpRequestData.method.value}], Url=[${httpRequestData.url}]", it)
                respondText(
                    status = HttpStatusCode.BadGateway,
                    text = "Unable to proxy request."
                )
            }
        )
    }

    private fun ApplicationCall.populateBuilder(
        builder: HttpRequestBuilder,
        body: ByteArray?,
        extraHeaders: Map<String, Any?>) {

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

        // Query parameters
        request.queryParameters.forEach { key, value ->
            builder.parameter(key, value)
        }

        // Headers
        val extra = extraHeaders.filterValues { it != null }.mapValues { it.value!! }
        val remove = extraHeaders.filterValues { it == null }
        request.headers.forEach { key, value ->
            if (key !in remove && !HttpHeaders.isUnsafe(key)) { builder.header(key, value) }
        }
        extra.forEach { (key, value) ->
            builder.header(key, value)
        }
    }
}