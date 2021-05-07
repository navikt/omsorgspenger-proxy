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

private val logger = LoggerFactory.getLogger("no.nav.K9SakRoute")
private const val Path = "/k9-sak"

internal fun Route.K9SakRoute(
  config: Config.K9Sak,
  stsClient: StsRestClient) {

    fun ApplicationCall.toUrl() =
      "${config.url}${request.uri.removePrefix(Path)}"

    fun accessToken() =
      stsClient.token().asAuthoriationHeader().let { mapOf(
        HttpHeaders.Authorization to it
      )}

    route(Path) {
        post("/fordel/journalposter") {
            call.forwardPost(call.toUrl(), accessToken(), logger)
        }

        post("/fordel/fagsak/opprett") {
            call.forwardPost(call.toUrl(), accessToken(), logger)
        }

        get("/internal/health/isReady") {
            call.forwardGet(call.toUrl(), emptyMap(), logger)
        }
    }
}
