package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.Route
import io.ktor.routing.options
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.omsorgspenger.NavConsumerToken
import no.nav.omsorgspenger.OpenAm
import no.nav.omsorgspenger.OpenAm.Companion.harOpenAmToken
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.erScopetTilOmsorgspengerProxy
import no.nav.omsorgspenger.forwardOptions
import no.nav.omsorgspenger.forwardPost
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.PdlRoute")

internal fun Route.PdlRoute(
    pdlConfig: Config.PDL,
    authConfig: Config.Auth,
    stsClient: StsRestClient,
    openAm: OpenAm
) {
    route("/pdl{...}") {
        post {
            val pdlUrl = pdlConfig.url
            val path = call.request.uri.removePrefix("/pdl")
            val fullPdlPath = "$pdlUrl$path"

            val stsAuthorizationHeader = stsClient.token().asAuthoriationHeader()
            val erScopetTilOmsorgspengerProxy = call.erScopetTilOmsorgspengerProxy(authConfig.azureAppClientId)

            val authorizationHeader = when {
                erScopetTilOmsorgspengerProxy && call.harOpenAmToken() -> openAm.verifisertHeaderValue(call)
                erScopetTilOmsorgspengerProxy -> stsAuthorizationHeader
                else -> call.request.headers[HttpHeaders.Authorization]!!
            }

            val extraHeaders = mapOf(
                HttpHeaders.Authorization to authorizationHeader,
                NavConsumerToken to stsAuthorizationHeader
            )

            call.forwardPost(fullPdlPath, extraHeaders, logger)
        }

        options {
            val pdlUrl = pdlConfig.url
            val path = call.request.uri.removePrefix("/pdl")
            val fullPdlPath = "$pdlUrl$path"

            val stsToken = stsClient.token().asAuthoriationHeader()

            val extraHeaders = mapOf(HttpHeaders.Authorization to stsToken)

            call.forwardOptions(fullPdlPath, extraHeaders, logger)
        }
    }
}