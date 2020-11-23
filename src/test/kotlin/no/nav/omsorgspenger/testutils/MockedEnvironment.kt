package no.nav.omsorgspenger.testutils

import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getNaisStsTokenUrl
import no.nav.omsorgspenger.testutils.mocks.TokenResponseTransformer
import no.nav.omsorgspenger.testutils.mocks.pdlUrl
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

    internal val appConfig = mutableMapOf<String, String>()

    init {
        appConfig["nav.sts.url"] = wireMockServer.getNaisStsTokenUrl()
        appConfig["nav.pdl.url"] = wireMockServer.pdlUrl()
        appConfig["nav.service_user.username"] = "test_username"
        appConfig["nav.service_user.password"] = "test_pw"
        appConfig["nav.auth.issuers.0.alias"] = "azure_proxy_scoped"
        appConfig["nav.auth.issuers.0.type"] = "azure"
        appConfig["nav.auth.issuers.0.audience"] = "omsorgspenger-proxy"
        appConfig["nav.auth.issuers.0.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
        appConfig["nav.auth.issuers.1.alias"] = "azure_any_scoped"
        appConfig["nav.auth.issuers.1.discovery_endpoint"] = wireMockServer.getAzureV2WellKnownUrl()
    }

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}
