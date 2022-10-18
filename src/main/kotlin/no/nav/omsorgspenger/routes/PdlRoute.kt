package no.nav.omsorgspenger.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.options
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import no.nav.omsorgspenger.FuelHttp.forwardOptions
import no.nav.omsorgspenger.FuelHttp.forwardPost
import no.nav.omsorgspenger.OpenAm
import no.nav.omsorgspenger.OpenAm.Companion.harOpenAmToken
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.PdlRoute")
private const val NavConsumerToken = "Nav-Consumer-Token"

internal fun Route.PdlRoute(
    pdlConfig: Config.PDL,
    stsClient: StsRestClient,
    openAm: OpenAm
) {
    route("/pdl{...}") {
        post {
            val pdlUrl = pdlConfig.url
            val path = call.request.uri.removePrefix("/pdl")
            val fullPdlPath = "$pdlUrl$path"

            if (!call.harOpenAmToken()) {
                logger.error("Requestet uten OpenAm-Token")
                return@post call.respond(HttpStatusCode.Forbidden)
            }

            val stsAuthorizationHeader = stsClient.token().asAuthoriationHeader()
            val authorizationHeader = openAm.verifisertHeaderValue(call)

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
