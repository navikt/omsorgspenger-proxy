package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.forwardGet
import no.nav.omsorgspenger.forwardPost
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("OppgaveRoute")

internal fun Route.OppgaveRoute(
    config: Config,
    stsClient: StsRestClient
) {
    route("/oppgave{...}") {
        val oppgaveUrl = config.oppgave.url

        post {
            val path = call.request.uri.removePrefix("/oppgave")
            val stsToken = stsClient.token().asAuthoriationHeader()
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken
            )

            call.forwardPost("$oppgaveUrl$path", extraHeaders, logger)
        }

        get {
            val stsToken = stsClient.token().asAuthoriationHeader()
            val path = call.request.uri.removePrefix("/oppgave")
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken
            )

            call.forwardGet("$oppgaveUrl$path", extraHeaders, logger)
        }
    }
}
