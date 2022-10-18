package no.nav.omsorgspenger.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.omsorgspenger.FuelHttp.forwardGet
import no.nav.omsorgspenger.FuelHttp.forwardPost
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.SafRoute")
private const val Path = "/saf"

internal fun Route.SafRoute(
    config: Config.SAF,
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
        post("/graphql") {
            call.forwardPost(call.toUrl(), accessToken(), logger)
        }

        get("/isReady") {
            call.forwardGet(call.toUrl(), utenAuthorizationHeader, logger)
        }
    }
}
