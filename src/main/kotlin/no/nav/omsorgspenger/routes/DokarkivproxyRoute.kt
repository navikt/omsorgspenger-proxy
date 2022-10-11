package no.nav.omsorgspenger.routes

import io.ktor.server.application.call
import io.ktor.http.HttpHeaders
import io.ktor.server.request.uri
import io.ktor.server.routing.*
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.KtorHttp.forwardPut
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.DokarkivproxyRoute")

internal fun Route.DokarkivproxyRoute(
    dokarkivProxyConfig: Config.Dokarkivproxy,
    stsClient: StsRestClient
) {

    fun headers() = mapOf(
        HttpHeaders.Authorization to stsClient.token().asAuthoriationHeader()
    )

    route("/dokarkivproxy{...}") {
        put {
            val dokarkivproxyUrl = dokarkivProxyConfig.url
            val path = call.request.uri.removePrefix("/dokarkivproxy")
            call.forwardPut("$dokarkivproxyUrl$path", headers())
        }

        get {
            val dokarkivproxyUrl = dokarkivProxyConfig.url
            val path = call.request.uri.removePrefix("/dokarkivproxy")
            call.forwardGet("$dokarkivproxyUrl$path", headers())
        }
    }
}