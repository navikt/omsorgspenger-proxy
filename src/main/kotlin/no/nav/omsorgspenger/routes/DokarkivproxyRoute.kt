package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.Route
import io.ktor.routing.put
import io.ktor.routing.route
import no.nav.omsorgspenger.NavConsumerToken
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.forwardPut
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.DokarkivproxyRoute")

internal fun Route.DokarkivproxyRoute(
    dokarkivProxyConfig: Config.Dokarkivproxy,
    stsClient: StsRestClient,
) {
    route("/dokarkivproxy{...}") {
        put {
            val dokarkivproxyUrl = dokarkivProxyConfig.url
            val path = call.request.uri.removePrefix("/dokarkivproxy")

            val stsToken = stsClient.token().asAuthoriationHeader()
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken,
                NavConsumerToken to stsToken
            )

            call.forwardPut("$dokarkivproxyUrl$path", extraHeaders, logger)
        }
    }
}