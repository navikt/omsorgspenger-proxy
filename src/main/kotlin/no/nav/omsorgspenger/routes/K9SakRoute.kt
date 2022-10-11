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

private val logger = LoggerFactory.getLogger("no.nav.K9SakRoute")
private const val Path = "/k9-sak"

internal fun Route.K9SakRoute(
    config: Config.K9Sak,
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

    val utenAuthorizationHeader = mapOf(HttpHeaders.Authorization to null)

    route(Path) {
        post("/api/fordel/journalposter") {
            call.forwardPost(call.toUrl(), accessToken())
        }

        post("/api/fordel/fagsak/opprett") {
            call.forwardPost(call.toUrl(), accessToken())
        }

        post("/api/fagsak/siste") {
            call.forwardPost(call.toUrl(), accessToken())
        }

        post("/api/fordel/psb-infotrygd/finnes") {
            call.forwardPost(call.toUrl(), accessToken())
        }

        post("/api/fagsak/match") {
            call.forwardPost(call.toUrl(), accessToken())
        }

        get("/internal/health/isReady") {
            call.forwardGet(call.toUrl(), utenAuthorizationHeader)
        }
    }
}
