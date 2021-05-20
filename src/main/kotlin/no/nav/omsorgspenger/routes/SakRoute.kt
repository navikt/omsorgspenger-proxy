package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.forwardGet
import no.nav.omsorgspenger.forwardPost
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.SakRoute")
private const val Path = "/sak"

internal fun Route.SakRoute(
    config: Config.Sak,
    stsClient: StsRestClient) {

    fun ApplicationCall.toUrl() =
        "${config.url}${request.uri.removePrefix(Path)}"

    fun accessToken() =
        stsClient.token().asAuthoriationHeader().let {
            mapOf(
                HttpHeaders.Authorization to it
            )
        }

    route("$Path{...}") {
        get {
            call.forwardGet(call.toUrl(), accessToken(), logger)
        }
        post {
            call.forwardPost(call.toUrl(), accessToken(), logger)
        }
    }
}
