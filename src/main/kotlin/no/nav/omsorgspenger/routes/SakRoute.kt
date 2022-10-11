package no.nav.omsorgspenger.routes

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.KtorHttp.forwardPost
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.SakRoute")
private const val Path = "/sak"

internal fun Route.SakRoute(
    config: Config.Sak,
    stsClient: StsRestClient
) {

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
            call.forwardGet(call.toUrl(), accessToken())
        }
        post {
            call.forwardPost(call.toUrl(), accessToken())
        }
    }
}
