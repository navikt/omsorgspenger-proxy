package no.nav.omsorgspenger.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.omsorgspenger.FuelHttp.forwardGet
import no.nav.omsorgspenger.FuelHttp.forwardPost
import no.nav.omsorgspenger.config.Config
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.InfotrygdGrunnlagPaaroerendeSykdomRoute")
private const val Path = "/infotrygd-grunnlag-paaroerende-sykdom"

internal fun Route.InfotrygdGrunnlagPaaroerendeSykdomRoute(
    config: Config.InfotrygdGrunnlagPaaroerendeSykdom
) {
    fun ApplicationCall.toUrl() =
        "${config.url}${request.uri.removePrefix(Path)}"

    get("$Path{...}") {
        call.forwardGet(call.toUrl(), emptyMap(), logger)
    }

    post("$Path{...}") {
        call.forwardPost(call.toUrl(), emptyMap(), logger)
    }
}
