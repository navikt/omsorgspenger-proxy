package no.nav.omsorgspenger.testutils

import no.nav.helse.dusseldorf.testsupport.wiremock.*
import no.nav.omsorgspenger.testutils.mocks.stubDokarkivproxy
import no.nav.omsorgspenger.testutils.mocks.stubOppgave
import no.nav.omsorgspenger.testutils.mocks.stubPdl

internal class MockedEnvironment(
    wireMockPort: Int = 8082
) {

    internal val wireMockServer = WireMockBuilder()
        .withPort(wireMockPort)
        .withAzureSupport()
        .withNaisStsSupport()
        .build()
        .stubPdl()
        .stubOppgave()
        .stubDokarkivproxy()

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}