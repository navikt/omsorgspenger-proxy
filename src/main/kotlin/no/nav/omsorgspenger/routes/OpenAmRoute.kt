package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.routing.*
import no.nav.omsorgspenger.OpenAm
import no.nav.omsorgspenger.KtorHttp.forwardGet

internal fun Route.OpenAmPublicRoute(
    openAm: OpenAm) {
    val jwksUri = openAm.jwksUri.toString()
    get("/open-am/keys") {
        call.forwardGet(toUrl = jwksUri, extraHeaders = emptyMap())
    }
}