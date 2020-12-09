package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.Route
import io.ktor.routing.put
import io.ktor.routing.route
import no.nav.omsorgspenger.NavConsumerToken
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.erScopetTilOmsorgspengerProxy
import no.nav.omsorgspenger.forwardPost
import no.nav.omsorgspenger.forwardPut
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.PdlRoute")

internal fun Route.DokarkivproxyRoute(
    config: Config,
    stsClient: StsRestClient,
) {
    route("/dokarkivproxy{...}") {
        put {
            val dokarkivproxyUrl = config.dokarkivproxy.url
            val path = call.request.uri.removePrefix("/dokarkivproxy")
            val fulldokarkivproxyUrl = "$dokarkivproxyUrl$path"

            val stsAuthorizationHeader = stsClient.token().asAuthoriationHeader()

            val authorizationHeader = if (call.erScopetTilOmsorgspengerProxy(config.auth.azureAppClientId))
                stsAuthorizationHeader
            else
                call.request.headers[HttpHeaders.Authorization]!!

            val extraHeaders = mapOf(
                HttpHeaders.Authorization to authorizationHeader,
            )

            call.forwardPut(fulldokarkivproxyUrl, extraHeaders, logger)
        }
    }
}