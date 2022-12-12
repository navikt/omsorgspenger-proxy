package no.nav.omsorgspenger.testutils

import no.nav.helse.dusseldorf.testsupport.wiremock.*

internal class MockedEnvironment {

    internal val wireMockServer = WireMockBuilder()
        .withAzureSupport()
        .withNaisStsSupport()
        .build()

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}
