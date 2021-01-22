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
import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.ldap.LdapService
import no.nav.omsorgspenger.routes.ActiveDirectoryRoute
import no.nav.omsorgspenger.routes.DokarkivproxyRoute
import no.nav.omsorgspenger.routes.OppgaveRoute
import no.nav.omsorgspenger.routes.PdlRoute
import no.nav.omsorgspenger.sts.StsRestClient
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
internal fun Application.app(applicationContext: ApplicationContext = ApplicationContext.Builder().build()) {
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
        authenticate(*issuers.azureAnyScoped()) {
            PdlRoute(
                pdlConfig = Config.PDL(applicationContext.env),
                authConfig = Config.Auth(applicationContext.env),
                stsClient = stsClient,
                openAm = openAm
            )

        }
        authenticate(*issuers.azureProxyScoped()) {
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
        }
    }
}
