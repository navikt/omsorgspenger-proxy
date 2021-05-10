package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer

internal fun WireMockServer.safUrl(): String = baseUrl() + "/saf-mock"