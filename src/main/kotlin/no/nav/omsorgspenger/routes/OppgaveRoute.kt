package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.request.receive
import io.ktor.request.uri
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.appendAll
import no.nav.omsorgspenger.addAndOverride
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.pipeResponse
import no.nav.omsorgspenger.sts.StsRestClient
import org.json.simple.JSONObject

internal fun Route.OppgaveRoute(
    httpClient: HttpClient,
    config: Config,
    stsClient: StsRestClient
) {
    route("/oppgave{...}") {
        val oppgaveUrl = config.oppgave.url

        post {
            val path = call.request.uri.removePrefix("/oppgave")
            val stsToken = stsClient.token()
            val headersBuilder = call.request.headers.addAndOverride(
                mapOf(
                    HttpHeaders.Authorization to "Bearer $stsToken"
                )
            )

            val response = httpClient.post<HttpResponse>("$oppgaveUrl$path") {
                headers.appendAll(headersBuilder)
                body = call.receive<JSONObject>()
            }
            call.pipeResponse(response)
        }

        get {
            val stsToken = stsClient.token()
            val path = call.request.uri.removePrefix("/oppgave")
            val headersBuilder = call.request.headers.addAndOverride(
                mapOf(
                    HttpHeaders.Authorization to "Bearer $stsToken"
                )
            )

            val response = httpClient.get<HttpResponse>("$oppgaveUrl$path") {
                headers.appendAll(headersBuilder)
            }
            call.pipeResponse(response)
        }
    }
}
