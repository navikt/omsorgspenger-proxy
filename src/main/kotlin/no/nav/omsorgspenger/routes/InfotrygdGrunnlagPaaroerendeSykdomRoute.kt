package no.nav.omsorgspenger.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.KtorHttp.forwardPost
import no.nav.omsorgspenger.config.Config

private const val Path = "/infotrygd-grunnlag-paaroerende-sykdom"

internal fun Route.InfotrygdGrunnlagPaaroerendeSykdomRoute(
    config: Config.InfotrygdGrunnlagPaaroerendeSykdom
) {
    fun ApplicationCall.toUrl() =
        "${config.url}${request.uri.removePrefix(Path)}"

    get("$Path{...}") {
        call.forwardGet(call.toUrl())
    }

    post("$Path{...}") {
        call.forwardPost(call.toUrl())
    }
}
