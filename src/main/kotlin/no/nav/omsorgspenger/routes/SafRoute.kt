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

private val logger = LoggerFactory.getLogger("no.nav.SafRoute")
private const val Path = "/saf"

internal fun Route.SafRoute(
    config: Config.SAF,
    stsClient: StsRestClient) {

    fun ApplicationCall.toUrl() =
        "${config.url}${request.uri.removePrefix(Path)}"

    fun accessToken() =
        stsClient.token().asAuthoriationHeader().let { mapOf(
            HttpHeaders.Authorization to it
        )
    }

    route (Path) {
        post("/graphql") {
            call.forwardPost(call.toUrl(), accessToken(), logger)
        }

        get("/isReady") {
            call.forwardGet(call.toUrl(), emptyMap(), logger)
        }
    }
}
