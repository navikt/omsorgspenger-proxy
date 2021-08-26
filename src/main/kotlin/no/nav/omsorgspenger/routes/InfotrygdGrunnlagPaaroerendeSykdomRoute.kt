package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import no.nav.omsorgspenger.KtorHttp.doGet
import no.nav.omsorgspenger.KtorHttp.forward
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.config.Config
import org.slf4j.LoggerFactory

private const val Path = "/infotrygd-grunnlag-paaroerende-sykdom"
private val logger = LoggerFactory.getLogger("no.nav.InfotrygdGrunnlagPaaroerendeSykdomRoute")

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
                logger.warn("Oppslag mot k9-infotrygd feilet. Fallbacker til infotrygd-grunnlag-paaroerende-sykdom")
                call.forwardGet(call.toUrl(), emptyMap())
            } else {
                logger.info("Oppslag mot k9-infotrygd gikk bra!")
            }
        }
    }
}