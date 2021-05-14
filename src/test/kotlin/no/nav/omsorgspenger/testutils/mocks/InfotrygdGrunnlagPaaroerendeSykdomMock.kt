package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer

internal fun WireMockServer.infotrygdGrunnlagPaaroerendeSykdomUrl(): String = baseUrl() + "/infotrygd-grunnlag-paaroerende-sykdom-mock"
