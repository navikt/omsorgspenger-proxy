package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.*
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.KtorHttp.forwardPost
import no.nav.omsorgspenger.KtorHttp.forwardPatch
import no.nav.omsorgspenger.sts.StsRestClient

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
