package no.nav.omsorgspenger.routes

import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.server.routing.*
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.config.Config
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

            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken,
                "Nav-Consumer-Token" to stsToken
            )

            call.forwardGet("$aaregUrl$path", extraHeaders)
        }
    }
}
