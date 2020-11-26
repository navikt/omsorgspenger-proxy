package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import no.nav.omsorgspenger.NavConsumerToken
import no.nav.omsorgspenger.config.Config
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
            logger.info("proxyer kall mot $fullPdlPath")
            val stsToken = stsClient.token()
            val jwt = call.principal<JWTPrincipal>()!!

            val authToken = if (jwt.erScopetTilOmsorgspengerProxy(config.auth.azureAppClientId))
                "Bearer $stsToken"
            else
                call.request.headers[HttpHeaders.Authorization]!!

            val extraHeaders = mapOf(
                HttpHeaders.Authorization to authToken,
                NavConsumerToken to "Bearer $stsToken"
            )

            call.forwardPost(fullPdlPath, extraHeaders, logger)
        }
    }
}

private fun JWTPrincipal.erScopetTilOmsorgspengerProxy(proxyClientId: String): Boolean =
    payload.audience.contains(proxyClientId)
