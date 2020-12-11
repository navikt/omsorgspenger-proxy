package no.nav.omsorgspenger.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

internal data class Config(
    val serviceUser: ServiceUser,
    val pdl: PDL,
    val oppgave: Oppgave,
    val sts: STS,
    val dokarkivproxy: Dokarkivproxy,
    val auth: Auth
) {
    internal data class ServiceUser(
        val username: String,
        val password: String
    )

    internal data class PDL(
        val url: String
    )

    internal data class Oppgave(
        val url: String
    )

    internal data class STS(
        val url: String
    )

    internal data class Dokarkivproxy(
        val url: String
    )

    internal data class Auth(
        val azureAppClientId: String
    )
}

@KtorExperimentalAPI
internal fun ApplicationConfig.load() = Config(
    serviceUser = Config.ServiceUser(
        username = property("nav.service_user.username").getString(),
        password = property("nav.service_user.password").getString()
    ),
    pdl = Config.PDL(
        url = property("nav.pdl.url").getString()
    ),
    oppgave = Config.Oppgave(
        url = property("nav.oppgave.url").getString()
    ),
    sts = Config.STS(
        url = property("nav.sts.url").getString()
    ),
    auth = Config.Auth(
        azureAppClientId = property("nav.auth.azure_app_client_id").getString()
    ),
    dokarkivproxy = Config.Dokarkivproxy(
        url = property("nav.dokarkivproxy.url").getString()
    )
)
