package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer

internal fun WireMockServer.sakUrl(): String = baseUrl() + "/sak-mock"
