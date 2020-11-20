package no.nav.omsorgspenger.testutils

import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.helse.dusseldorf.testsupport.wiremock.getNaisStsTokenUrl

internal class MockedEnvironment(
    wireMockPort: Int = 8082
) {

    internal val wireMockServer = WireMockBuilder()
        .withPort(wireMockPort)
        .withAzureSupport()
        .withNaisStsSupport()
        .build()

    internal val appConfig = mutableMapOf<String, String>()

    init {
        appConfig["nav.sts.url"] = wireMockServer.getNaisStsTokenUrl()
        appConfig["nav.pdl.url"] = "TODO"
        appConfig["nav.service_user.username"] = "test_username"
        appConfig["nav.service_user.password"] = "test_pw"
    }

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}
