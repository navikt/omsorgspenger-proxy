package no.nav.omsorgspenger

import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponseResult
import io.ktor.server.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import org.slf4j.Logger

internal object FuelHttp {
    internal suspend fun ApplicationCall.forwardPost(toUrl: String, extraHeaders: Map<String, Any?>, logger: Logger) {
        val parameters = request.queryParameters.toFuel()
        val postRequest = toUrl
            .httpPost(parameters)
            .body(receive<ByteArray>())

        forwardRequest(postRequest, extraHeaders, logger)
    }

    internal suspend fun ApplicationCall.forwardGet(toUrl: String, extraHeaders: Map<String, Any?>, logger: Logger) {
        val parameters = request.queryParameters.toFuel()
        val postRequest = toUrl
            .httpGet(parameters)

        forwardRequest(postRequest, extraHeaders, logger)
    }

    private suspend fun ApplicationCall.forwardRequest(fuelRequest: Request, extraHeaders: Map<String, Any?>, logger: Logger) {
        val httpRequest = fuelRequest
            .header(request.headers.toMap(extraHeaders))
            .timeout(20_000)
            .timeoutRead(20_000)

        val (_, response, result) = httpRequest.awaitByteArrayResponseResult()
        result.fold(
            { success -> forward(response, success) },
            { failure ->
                if (-1 == response.statusCode) {
                    logger.error(failure.toString())
                    respondErrorAndLog(HttpStatusCode.GatewayTimeout, "Unable to proxy request.", logger)
                } else {
                    forward(response, failure.errorData)
                }
            }
        )
    }

    private fun Headers.toMap(extraHeaders: Map<String, Any?>): Map<String, Any> {
        val fuelHeaders = mutableMapOf<String, Any>()
        forEach { key, values ->
            fuelHeaders[key] = values
        }
        val extra = extraHeaders.filterValues { it != null }.mapValues { it.value!! }
        val remove = extraHeaders.filterValues { it == null }.keys

        extra.forEach {
            fuelHeaders[it.key] = it.value
        }
        remove.forEach {
            fuelHeaders.remove(it)
        }

        return fuelHeaders.toMap()
    }

    private fun Parameters.toFuel(): List<Pair<String, Any?>> {
        val fuelParameters = mutableListOf<Pair<String, Any?>>()
        forEach { key, value ->
            value.forEach { fuelParameters.add(key to it) }
        }
        return fuelParameters.toList()
    }

    private suspend fun ApplicationCall.forward(
        clientResponse: Response,
        body: ByteArray
    ) {
        clientResponse.headers.forEach { key, value ->
            if (!HttpHeaders.isUnsafe(key)) {
                value.forEach { response.header(key, it) }
            }
        }
        respondBytes(
            bytes = body,
            status = HttpStatusCode.fromValue(clientResponse.statusCode),
            contentType = clientResponse.contentType()
        )
    }

    private fun Response.contentType(): ContentType {
        val clientContentTypesHeaders = header(HttpHeaders.ContentType)
        return if (clientContentTypesHeaders.isEmpty()) ContentType.Text.Plain else ContentType.parse(clientContentTypesHeaders.first())
    }

    private suspend fun ApplicationCall.respondErrorAndLog(status: HttpStatusCode, error: String, logger: Logger) {
        logger.error("HTTP $status -> $error")
        respond(status, error)
    }
}