package no.nav.omsorgspenger

import io.ktor.application.ApplicationCall
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.response.respond
import io.ktor.util.filter
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.copyAndClose

suspend fun ApplicationCall.pipeResponse(response: HttpResponse) {
    val proxiedHeaders = response.headers
    val contentType = proxiedHeaders[HttpHeaders.ContentType]

    respond(object : OutgoingContent.WriteChannelContent() {
        override val contentType: ContentType? = contentType?.let { ContentType.parse(it) }
        override val headers: Headers = Headers.build {
            appendAll(
                proxiedHeaders.filter { key, _ ->
                    !key.equals(HttpHeaders.ContentType, ignoreCase = true) &&
                        !key.equals(HttpHeaders.ContentLength, ignoreCase = true) &&
                        !key.equals(HttpHeaders.TransferEncoding, ignoreCase = true)
                }
            )
        }
        override val status: HttpStatusCode? = response.status
        override suspend fun writeTo(channel: ByteWriteChannel) {
            response.content.copyAndClose(channel)
        }
    })
}

internal fun Headers.addAndOverride(headers: Map<String, String>): HeadersBuilder {
    val proxiedHeaders = filter { key, _ ->
        !headers.containsKey(key)
    }

    val headersBuilder = HeadersBuilder()
    headersBuilder.appendAll(proxiedHeaders)
    headers.forEach {
        headersBuilder.append(it.key, it.value)
    }

    return headersBuilder
}

val NavCallId: String
    get() = "Nav-Call-Id"
val NavConsumerId: String
    get() = "Nav-Consumer-Id"
val NavConsumerToken: String
    get() = "Nav-Consumer-Token"
