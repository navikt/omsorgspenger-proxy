package no.nav.omsorgspenger.testutils

import no.nav.helse.dusseldorf.testsupport.wiremock.*
import no.nav.omsorgspenger.testutils.mocks.TokenResponseTransformer
import no.nav.omsorgspenger.testutils.mocks.dokarkivproxyUrl
import no.nav.omsorgspenger.testutils.mocks.oppgaveUrl
import no.nav.omsorgspenger.testutils.mocks.pdlUrl
import no.nav.omsorgspenger.testutils.mocks.stubDokarkivproxy
import no.nav.omsorgspenger.testutils.mocks.stubOppgave
import no.nav.omsorgspenger.testutils.mocks.stubPdl
import no.nav.omsorgspenger.testutils.mocks.tokenTransformerMatcher

internal class MockedEnvironment(
    wireMockPort: Int = 8082
) {

    internal val wireMockServer = WireMockBuilder()
        .withPort(wireMockPort)
        .withAzureSupport()
        .withNaisStsSupport()
        .wireMockConfiguration {
            it.extensions(TokenResponseTransformer(tokenTransformerMatcher))
        }
        .build()
        .stubPdl()
        .stubOppgave()
        .stubDokarkivproxy()

    internal val appConfig = mutableMapOf<String, String>()

    init {
        appConfig["nav.sts.url"] = wireMockServer.getNaisStsTokenUrl()
        appConfig["nav.pdl.url"] = wireMockServer.pdlUrl()
        appConfig["nav.oppgave.url"] = wireMockServer.oppgaveUrl()
        appConfig["nav.dokarkivproxy.url"] = wireMockServer.dokarkivproxyUrl()
        appConfig["nav.service_user.username"] = "test_username"
        appConfig["nav.service_user.password"] = "test_pw"
        appConfig["nav.open_am.discovery_endpoint"] = wireMockServer.getNaisStsWellKnownUrl()
        appConfig["nav.auth.issuers.0.alias"] = "azure_proxy_scoped"
        appConfig["nav.auth.issuers.0.type"] = "azure"
        appConfig["nav.auth.issuers.0.audience"] = azureAppClientId
        appConfig["nav.auth.issuers.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
        appConfig["nav.auth.issuers.1.alias"] = "azure_any_scoped"
        appConfig["nav.auth.issuers.1.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
        appConfig["nav.auth.azure_app_client_id"] = azureAppClientId
        appConfig["nav.auth.azure_app_authorized_client_ids"] = "allowed-1,allowed-2"
    }

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}

internal const val azureAppClientId = "omsorgspenger-proxy"
