package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.KtorHttp.forwardPost
import no.nav.omsorgspenger.sts.StsRestClient

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

    val utenAuthorizationHeader = mapOf(HttpHeaders.Authorization to null)


    route (Path) {
        post("/graphql") {
            call.forwardPost(call.toUrl(), accessToken())
        }

        get("/isReady") {
            call.forwardGet(call.toUrl(), utenAuthorizationHeader)
        }
    }
}
