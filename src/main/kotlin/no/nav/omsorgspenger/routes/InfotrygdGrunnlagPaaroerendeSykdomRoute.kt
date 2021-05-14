package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.forwardGet
import org.slf4j.LoggerFactory

private const val Path = "/infotrygd-grunnlag-paaroerende-sykdom"
private val logger = LoggerFactory.getLogger("no.nav.InfotrygdGrunnlagPaaroerendeSykdomRoute")

internal fun Route.InfotrygdGrunnlagPaaroerendeSykdomRoute(
    config: Config.InfotrygdGrunnlagPaaroerendeSykdom) {

    fun ApplicationCall.toUrl() =
        "${config.url}${request.uri.removePrefix(Path)}"

    get("$Path{...}") {
        call.forwardGet(call.toUrl(), emptyMap(), logger)
    }
}