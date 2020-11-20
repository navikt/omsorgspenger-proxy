package no.nav.omsorgspenger

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.CallId
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.auth.AuthStatusPages
import no.nav.helse.dusseldorf.ktor.core.DefaultProbeRoutes
import no.nav.helse.dusseldorf.ktor.core.DefaultStatusPages
import no.nav.helse.dusseldorf.ktor.core.fromXCorrelationIdHeader
import no.nav.omsorgspenger.config.load
import no.nav.omsorgspenger.routes.PdlRoute
import no.nav.omsorgspenger.sts.StsRestClient

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
fun Application.app() {
    val config = runBlocking { environment.config.load() }
    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        DefaultStatusPages()
        AuthStatusPages()
    }

    install(CallId) {
        fromXCorrelationIdHeader(
            generateOnInvalid = true
        )
    }

    val httpClient = HttpClient {
        install(JsonFeature)
    }

    val stsClient = StsRestClient(
        stsTokenUrl = config.sts.url,
        serviceUser = config.serviceUser,
        httpClient = httpClient
    )

    install(Routing) {
        DefaultProbeRoutes()
        PdlRoute(
            config = config,
            stsClient = stsClient,
            httpClient = httpClient
        )
    }
}
