package no.nav.omsorgspenger

import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import no.nav.helse.dusseldorf.ktor.auth.AuthStatusPages
import no.nav.helse.dusseldorf.ktor.auth.multipleJwtIssuers
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.omsorgspenger.Auth.azureAnyScoped
import no.nav.omsorgspenger.Auth.azureProxyScoped
import no.nav.omsorgspenger.Auth.omsorgspengerProxyIssuers
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.ldap.LdapService
import no.nav.omsorgspenger.routes.*
import no.nav.omsorgspenger.routes.ActiveDirectoryRoute
import no.nav.omsorgspenger.routes.DokarkivproxyRoute
import no.nav.omsorgspenger.routes.OppgaveRoute
import no.nav.omsorgspenger.sts.StsRestClient

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

internal fun Application.app(applicationContext: ApplicationContext = ApplicationContext.Builder().build()) {
    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        DefaultStatusPages()
        AuthStatusPages()
    }

    install(CallId) {
        fromFirstNonNullHeader(headers = listOf(
            HttpHeaders.XCorrelationId,
            "Nav-Call-Id",
            "Nav-CallId"
        ))
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests() // Fikser fancy color logging som printar ansi koder i log output.
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

    val openAm = OpenAm(
        openAmConfig = Config.OpenAM(applicationContext.env)
    )

    install(Routing) {
        HealthRoute(healthService = healthService)
        MetricsRoute()
        DefaultProbeRoutes()
        OpenAmPublicRoute(openAm = openAm)
        authenticate(*issuers.azureAnyScoped()) {
            // Underliggende tjenester støtter Azure-tokens, men ikke tilgjengeliggjort i GCP
            InfotrygdGrunnlagPaaroerendeSykdomRoute(
                config = Config.InfotrygdGrunnlagPaaroerendeSykdom(applicationContext.env)
            )
        }
        authenticate(*issuers.azureProxyScoped()) {
            // Underliggende tjenester støtter ikke Azure-tokens, veksler til tokens de støtter.
            OppgaveRoute(
                oppgaveConfig = Config.Oppgave(applicationContext.env),
                stsClient = stsClient
            )
            DokarkivproxyRoute(
                dokarkivProxyConfig = Config.Dokarkivproxy(applicationContext.env),
                stsClient = stsClient
            )
            ActiveDirectoryRoute(
                openAm = openAm,
                ldapService = LdapService(
                    ldapGateway = applicationContext.ldapGateway
                )
            )
            K9SakRoute(
                config = Config.K9Sak(applicationContext.env),
                stsClient = stsClient
            )
            SafRoute(
                config = Config.SAF(applicationContext.env),
                stsClient = stsClient
            )
            SakRoute(
                config = Config.Sak(applicationContext.env),
                stsClient = stsClient
            )
        }
    }
}
