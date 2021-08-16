package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.*
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.FuelHttp.forwardGet
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
                HttpHeaders.Authorization to stsToken
            )

            call.forwardGet("$aaregUrl$path", extraHeaders, logger)
        }
    }
}
