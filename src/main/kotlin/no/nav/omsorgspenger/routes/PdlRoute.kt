package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.omsorgspenger.NavCallId
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.pipeResponse
import no.nav.omsorgspenger.sts.StsRestClient
import org.json.simple.JSONObject
import java.util.UUID

internal fun Route.PdlRoute(
    config: Config,
    stsClient: StsRestClient,
    httpClient: HttpClient
) {
    route("/pdl") {
        post {
            val pdlUrl = config.pdl.url
            // TODO: hent access token fra request. hvis scopet til proxy, veksle med sts token. hvis ikke, propager tokenet
            val stsToken = stsClient.token()
            val callId = call.request.header(NavCallId) ?: UUID.randomUUID().toString()

            val response = httpClient.post<HttpResponse>(pdlUrl) {
                header(HttpHeaders.Authorization, "Bearer $stsToken")
                header("Nav-Consumer-Token", "Bearer $stsToken")
                header(HttpHeaders.XCorrelationId, callId)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                body = call.receive<JSONObject>()
            }
            call.pipeResponse(response)
        }
    }
}
