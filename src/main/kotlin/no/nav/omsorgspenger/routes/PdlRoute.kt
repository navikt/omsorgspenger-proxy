package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.appendAll
import io.ktor.util.filter
import no.nav.omsorgspenger.NavConsumerToken
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.pipeResponse
import no.nav.omsorgspenger.plus
import no.nav.omsorgspenger.plusAll
import no.nav.omsorgspenger.sts.StsRestClient
import org.json.simple.JSONObject

internal fun Route.PdlRoute(
    config: Config,
    stsClient: StsRestClient,
    httpClient: HttpClient
) {
    route("/pdl") {
        post {
            val pdlUrl = config.pdl.url
            val stsToken = stsClient.token()
            val jwt = call.principal<JWTPrincipal>()!!

            val authToken = if (jwt.erScopetTilOmsorgspengerProxy(config.auth.azureAppClientId))
                "Bearer $stsToken"
            else
                call.request.headers[HttpHeaders.Authorization]!!

            val proxiedHeaders = call.request.headers.filter { key, _ ->
                !key.equals(HttpHeaders.Authorization, ignoreCase = true) &&
                    !key.equals(NavConsumerToken, ignoreCase = true)
            }
            val headersBuilder = HeadersBuilder()
                .plusAll(proxiedHeaders)
                .plus(HttpHeaders.Authorization, authToken)
                .plus(NavConsumerToken, "Bearer $stsToken")

            val response = httpClient.post<HttpResponse>(pdlUrl) {
                headers.appendAll(headersBuilder)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                body = call.receive<JSONObject>()
            }
            call.pipeResponse(response)
        }
    }
}

private fun JWTPrincipal.erScopetTilOmsorgspengerProxy(proxyClientId: String): Boolean =
    payload.audience.contains(proxyClientId)
