package no.nav.omsorgspenger

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import no.nav.helse.dusseldorf.ktor.auth.AuthStatusPages
import no.nav.helse.dusseldorf.ktor.auth.multipleJwtIssuers
import no.nav.helse.dusseldorf.ktor.core.DefaultProbeRoutes
import no.nav.helse.dusseldorf.ktor.core.DefaultStatusPages
import no.nav.helse.dusseldorf.ktor.core.correlationIdAndRequestIdInMdc
import no.nav.helse.dusseldorf.ktor.core.fromFirstNonNullHeader
import no.nav.helse.dusseldorf.ktor.core.id
import no.nav.helse.dusseldorf.ktor.core.logRequests
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.omsorgspenger.Auth.azureAnyScoped
import no.nav.omsorgspenger.Auth.azureProxyScoped
import no.nav.omsorgspenger.Auth.omsorgspengerProxyIssuers
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.routes.InfotrygdGrunnlagPaaroerendeSykdomRoute
import no.nav.omsorgspenger.routes.K9SakRoute
import no.nav.omsorgspenger.routes.SakRoute
import no.nav.omsorgspenger.sts.StsRestClient

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

internal fun Application.app(
    applicationContext: ApplicationContext = ApplicationContext.Builder().build()
) {
    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        DefaultStatusPages()
        AuthStatusPages()
    }

    install(CallId) {
        fromFirstNonNullHeader(
            headers = listOf(
                HttpHeaders.XCorrelationId,
                "Nav-Call-Id",
                "Nav-CallId"
            )
        )
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests() // Fikser fancy color logging som printar ansi koder i log output.
        callIdMdc("callId")
    }

    val issuers = applicationContext.env.omsorgspengerProxyIssuers()

    install(Authentication) {
        multipleJwtIssuers(issuers)
    }

    val stsClient = StsRestClient(
        stsConfig = Config.STS(applicationContext.env),
        serviceUserConfig = Config.ServiceUser(applicationContext.env)
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
            // Underliggende tjenester støtter Azure-tokens, men ikke tilgjengeliggjort i GCP
            InfotrygdGrunnlagPaaroerendeSykdomRoute(
                config = Config.InfotrygdGrunnlagPaaroerendeSykdom(applicationContext.env)
            )
        }
        authenticate(*issuers.azureProxyScoped()) {
            // Underliggende tjenester støtter ikke Azure-tokens, veksler til tokens de støtter.
            K9SakRoute(
                config = Config.K9Sak(applicationContext.env),
                stsClient = stsClient
            )
            SakRoute(
                config = Config.Sak(applicationContext.env),
                stsClient = stsClient
            )
        }
    }
}
