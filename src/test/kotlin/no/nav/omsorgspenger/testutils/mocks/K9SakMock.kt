package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer

internal fun WireMockServer.k9SakUrl(): String = baseUrl() + "/k9-sak-mock"
