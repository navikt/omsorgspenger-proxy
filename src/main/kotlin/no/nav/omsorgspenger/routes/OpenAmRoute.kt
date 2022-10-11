package no.nav.omsorgspenger.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.omsorgspenger.KtorHttp.forwardGet
import no.nav.omsorgspenger.OpenAm
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.OpenAmPublicRoute")

internal fun Route.OpenAmPublicRoute(
    openAm: OpenAm
) {
    val jwksUri = openAm.jwksUri.toString()
    get("/open-am/keys") {
        call.forwardGet(toUrl = jwksUri, extraHeaders = emptyMap())
    }
}
