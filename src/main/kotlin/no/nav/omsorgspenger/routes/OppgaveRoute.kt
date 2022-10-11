package no.nav.omsorgspenger.routes

import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.server.routing.*
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.KtorHttp.forwardPatch
import no.nav.omsorgspenger.KtorHttp.forwardPost
import no.nav.omsorgspenger.config.Config
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

            call.forwardPost("$oppgaveUrl$path", extraHeaders)
        }

        patch {
            val path = call.request.uri.removePrefix("/oppgave")
            val stsToken = stsClient.token().asAuthoriationHeader()
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken
            )

            call.forwardPatch("$oppgaveUrl$path", extraHeaders)
        }

        get {
            val stsToken = stsClient.token().asAuthoriationHeader()
            val path = call.request.uri.removePrefix("/oppgave")
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken
            )

            call.forwardGet("$oppgaveUrl$path", extraHeaders)
        }
    }
}
