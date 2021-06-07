package no.nav.omsorgspenger.routes

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.uri
import io.ktor.routing.*
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.forwardGet
import no.nav.omsorgspenger.forwardPut
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.DokarkivproxyRoute")

internal fun Route.DokarkivproxyRoute(
    dokarkivProxyConfig: Config.Dokarkivproxy,
    stsClient: StsRestClient) {
    val utenAuthorizationHeader = mapOf(HttpHeaders.Authorization to null)

    route("/dokarkivproxy{...}") {
        put {
            val dokarkivproxyUrl = dokarkivProxyConfig.url
            val path = call.request.uri.removePrefix("/dokarkivproxy")

            val stsToken = stsClient.token().asAuthoriationHeader()
            val extraHeaders = mapOf(
                HttpHeaders.Authorization to stsToken
            )

            call.forwardPut("$dokarkivproxyUrl$path", extraHeaders, logger)
        }

        get("/isReady") {
            val dokarkivproxyUrl = dokarkivProxyConfig.url
            call.forwardGet("$dokarkivproxyUrl/isReady", utenAuthorizationHeader, logger)
        }
    }
}