package no.nav.omsorgspenger.sts

import no.nav.helse.dusseldorf.ktor.health.HealthCheck
import no.nav.helse.dusseldorf.ktor.health.Healthy
import no.nav.helse.dusseldorf.ktor.health.UnHealthy
import no.nav.helse.dusseldorf.oauth2.client.AccessToken
import no.nav.helse.dusseldorf.oauth2.client.CachedAccessTokenClient
import no.nav.helse.dusseldorf.oauth2.client.ClientSecretAccessTokenClient
import no.nav.omsorgspenger.config.Config
import java.net.URI

internal class StsRestClient(
    stsTokenUrl: String,
    serviceUser: Config.ServiceUser,
) : HealthCheck {

    private val cachedAccessTokenClient: CachedAccessTokenClient = CachedAccessTokenClient(
        ClientSecretAccessTokenClient(
            clientId = serviceUser.username,
            clientSecret = serviceUser.password,
            tokenEndpoint = URI(stsTokenUrl)
        )
    )

    internal fun token(): AccessToken {
        return cachedAccessTokenClient.getAccessToken(setOf("openid"))
    }

    override suspend fun check() = kotlin.runCatching {
        token()
    }.fold(
        onSuccess = { Healthy("StsRestClient", "OK") },
        onFailure = { UnHealthy("StsRestClient", "Feil: ${it.message}") }
    )
}
