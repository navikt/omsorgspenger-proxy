package no.nav.omsorgspenger.testutils

import no.nav.helse.dusseldorf.testsupport.wiremock.*
import no.nav.omsorgspenger.testutils.mocks.stubDokarkivproxy
import no.nav.omsorgspenger.testutils.mocks.stubOppgave

internal class MockedEnvironment(
    wireMockPort: Int = 8082
) {

    internal val wireMockServer = WireMockBuilder()
        .withPort(wireMockPort)
        .withAzureSupport()
        .withNaisStsSupport()
        .build()
        .stubOppgave()
        .stubDokarkivproxy()

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}