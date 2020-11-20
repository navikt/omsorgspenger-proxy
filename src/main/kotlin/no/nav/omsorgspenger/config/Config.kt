package no.nav.omsorgspenger.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import java.util.Base64

internal data class Config(
    val serviceUser: ServiceUser,
    val pdl: PDL,
    val sts: STS
) {
    internal data class ServiceUser(
        val username: String,
        val password: String
    ) {
        val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray(Charsets.UTF_8))}"

        override fun toString(): String {
            return "username:$username,password:$<SECRET>"
        }
    }

    internal data class PDL(
        val url: String
    )

    internal data class STS(
        val url: String
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
    sts = Config.STS(
        url = property("nav.sts.url").getString()
    )
)
