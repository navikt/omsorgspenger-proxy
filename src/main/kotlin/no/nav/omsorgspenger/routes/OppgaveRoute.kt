package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.*
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.forwardGet
import no.nav.omsorgspenger.forwardPatch
import no.nav.omsorgspenger.forwardPost
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.OppgaveRoute")

internal fun Route.OppgaveRoute(
    oppgaveConfig: Config.Oppgave,
    stsClient: StsRestClient
) {
    route("/oppgave{...}") {
        val oppgaveUrl = oppgaveConfig.url

        post {
            val path = call.request.uri.removePrefix("/oppgave")
            val stsToken = stsClient.token().asAuthoriationHeader()
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken
            )

            call.forwardPost("$oppgaveUrl$path", extraHeaders, logger)
        }

        patch {
            val path = call.request.uri.removePrefix("/oppgave")
            val stsToken = stsClient.token().asAuthoriationHeader()
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken
            )

            call.forwardPatch("$oppgaveUrl$path", extraHeaders, logger)
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
