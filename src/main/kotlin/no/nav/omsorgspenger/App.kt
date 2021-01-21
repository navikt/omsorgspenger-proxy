package no.nav.omsorgspenger

import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.request.*
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.ktor.auth.AuthStatusPages
import no.nav.helse.dusseldorf.ktor.auth.multipleJwtIssuers
import no.nav.helse.dusseldorf.ktor.core.DefaultProbeRoutes
import no.nav.helse.dusseldorf.ktor.core.DefaultStatusPages
import no.nav.helse.dusseldorf.ktor.core.id
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.omsorgspenger.Auth.azureAnyScoped
import no.nav.omsorgspenger.Auth.azureProxyScoped
import no.nav.omsorgspenger.Auth.omsorgspengerProxyIssuers
import no.nav.omsorgspenger.config.load
import no.nav.omsorgspenger.routes.DokarkivproxyRoute
import no.nav.omsorgspenger.routes.OppgaveRoute
import no.nav.omsorgspenger.routes.PdlRoute
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.event.Level
import java.net.URI

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
fun Application.app() {
    val config = environment.config.load()
    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        DefaultStatusPages()
        AuthStatusPages()
    }

    install(CallId) {
        retrieve { it.getCorrelationId() }
    }

    install(CallLogging) {
        val ignorePaths = setOf("/isalive", "/isready", "/metrics")
        level = Level.INFO
        logger = log
        filter { call -> !ignorePaths.contains(call.request.path().toLowerCase()) }
        callIdMdc("correlation_id")
    }

    val issuers = environment.config.omsorgspengerProxyIssuers()

    install(Authentication) {
        multipleJwtIssuers(issuers)
    }

    val stsClient = StsRestClient(
        stsTokenUrl = config.sts.url,
        serviceUser = config.serviceUser
    )
    val healthService = HealthService(
        setOf(
            stsClient
        )
    )

    HealthReporter(
        environment.config.id(),
        healthService
    )

    install(Routing) {
        HealthRoute(healthService = healthService)
        MetricsRoute()
        DefaultProbeRoutes()
        authenticate(*issuers.azureAnyScoped()) {
            PdlRoute(
                config = config,
                stsClient = stsClient,
                openAm = OpenAm(wellKnownUri = config.openAm.wellKnownUri)
            )

        }
        authenticate(*issuers.azureProxyScoped()) {
            OppgaveRoute(
                config = config,
                stsClient = stsClient
            )
            DokarkivproxyRoute(
                config = config,
                stsClient = stsClient
            )
        }
    }
}
