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

private val logger = LoggerFactory.getLogger("no.nav.AaregRoute")

internal fun Route.AaregRoute(
    aaregConfig: Config.AAREG,
    stsClient: StsRestClient
) {
    route("/aareg{...}") {
        val aaregUrl = aaregConfig.url

        get {
            val stsToken = stsClient.token().asAuthoriationHeader()
            val path = call.request.uri.removePrefix("/aareg")
            val personIdent = call.request.headers["Nav-Personident"]
            val maskinportenToken = call.request.headers["Authorization"]

            val extraHeaders = mapOf(
                HttpHeaders.Authorization to maskinportenToken,
                "Nav-Consumer-Token" to stsToken,
                "Nav-Personident" to personIdent
            )

            call.forwardGet("$aaregUrl$path", extraHeaders, logger)
        }
    }
}
