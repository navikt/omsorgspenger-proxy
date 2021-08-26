package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import no.nav.omsorgspenger.KtorHttp.doGet
import no.nav.omsorgspenger.KtorHttp.forward
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.config.Config

private const val Path = "/infotrygd-grunnlag-paaroerende-sykdom"

internal fun Route.InfotrygdGrunnlagPaaroerendeSykdomRoute(
    config: Config.InfotrygdGrunnlagPaaroerendeSykdom) {

    fun ApplicationCall.toUrl() =
        "${config.url}${request.uri.removePrefix(Path)}"

    fun ApplicationCall.toK9InfotygdUrl() =
        "${config.k9InfotrygdUrl}${request.uri.removePrefix(Path)}"

    get("$Path{...}") {
        if (config.sammeUrl) {
            call.forwardGet(call.toUrl(), emptyMap())
        } else {
            val success = call.forward(respondOnError = false) {
                call.doGet(call.toK9InfotygdUrl())
            }
            if (!success) {
                call.forwardGet(call.toUrl(), emptyMap())
            }
        }
    }
}