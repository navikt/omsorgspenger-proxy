package no.nav.omsorgspenger.routes

import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.uri
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.omsorgspenger.NavConsumerToken
import no.nav.omsorgspenger.addAndOverride
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("PdlRoute")

internal fun Route.PdlRoute(
    config: Config,
    stsClient: StsRestClient,
) {
    route("/pdl{...}") {
        post {
            val pdlUrl = config.pdl.url
            val path = call.request.uri.removePrefix("/pdl")
            val fullPdlPath = "$pdlUrl$path"
            logger.info("proxyer kall mot $fullPdlPath")
            val stsToken = stsClient.token()
            val jwt = call.principal<JWTPrincipal>()!!

            val authToken = if (jwt.erScopetTilOmsorgspengerProxy(config.auth.azureAppClientId))
                "Bearer $stsToken"
            else
                call.request.headers[HttpHeaders.Authorization]!!

            val headersBuilder = call.request.headers.addAndOverride(
                mapOf(
                    HttpHeaders.Authorization to authToken,
                    NavConsumerToken to "Bearer $stsToken"
                )
            )
            logger.info("request headers: ${headersBuilder.names().joinToString()}")
            val httpRequest = fullPdlPath
                .httpPost()
                .header(
                    call.request.headers.toFuel(
                        mapOf(
                            HttpHeaders.Authorization to authToken,
                            NavConsumerToken to "Bearer $stsToken"
                        )
                    )
                )
                .timeout(20_000)
                .timeoutRead(20_000)
                .body(call.receive<ByteArray>())

            val (_, response, result) = httpRequest.awaitByteArrayResponseResult()
            result.fold(
                { success -> call.forward(response, success) },
                { failure ->
                    if (-1 == response.statusCode) {
                        logger.error(failure.toString())
                        call.respondErrorAndLog(HttpStatusCode.GatewayTimeout, "Unable to proxy request.")
                    } else {
                        call.forward(response, failure.errorData)
                    }
                }
            )
//            val response = httpClient.post<HttpResponse>(fullPdlPath) {
//                headers.appendAll(headersBuilder)
//                body = call.receiveStream()
//            }
//            logger.info("pdl response headers: ${response.headers.names().joinToString()}")
//            logger.info("status fra pdl: ${response.status.value}")
//            call.pipeResponse(response)
        }
    }
}

private fun JWTPrincipal.erScopetTilOmsorgspengerProxy(proxyClientId: String): Boolean =
    payload.audience.contains(proxyClientId)

private fun Headers.toFuel(extraHeaders: Map<String, Any>): Map<String, Any> {
    val fuelHeaders = mutableMapOf<String, Any>()
    forEach { key, values ->
        fuelHeaders[key] = values
    }
    extraHeaders.forEach {
        fuelHeaders[it.key] = it.value
    }
    return fuelHeaders.toMap()
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

private suspend fun ApplicationCall.respondErrorAndLog(status: HttpStatusCode, error: String) {
    logger.error("HTTP $status -> $error")
    respond(status, error)
}
