package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.Route
import io.ktor.routing.options
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.omsorgspenger.NavConsumerToken
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.erScopetTilOmsorgspengerProxy
import no.nav.omsorgspenger.forwardOptions
import no.nav.omsorgspenger.forwardPost
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

            val stsAuthorizationHeader = stsClient.token().asAuthoriationHeader()

            val authorizationHeader = if (call.erScopetTilOmsorgspengerProxy(config.auth.azureAppClientId))
                stsAuthorizationHeader
            else
                call.request.headers[HttpHeaders.Authorization]!!

            val extraHeaders = mapOf(
                HttpHeaders.Authorization to authorizationHeader,
                NavConsumerToken to stsAuthorizationHeader
            )

            call.forwardPost(fullPdlPath, extraHeaders, logger)
        }

        options {
            val pdlUrl = config.pdl.url
            val path = call.request.uri.removePrefix("/pdl")
            val fullPdlPath = "$pdlUrl$path"

            val stsToken = stsClient.token().asAuthoriationHeader()

            val extraHeaders = mapOf(HttpHeaders.Authorization to stsToken)

            call.forwardOptions(fullPdlPath, extraHeaders, logger)
        }
    }
}