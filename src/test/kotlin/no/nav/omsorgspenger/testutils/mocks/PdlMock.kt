package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.*

private const val pdlUrlPath = "/pdl-mock"

internal const val ProxiedHeader = "Proxiedheader"
internal const val PdlOk = "hello-from-pdl"

internal fun WireMockServer.stubPdl(): WireMockServer {
    WireMock.stubFor(
        WireMock.post(WireMock.urlPathMatching(".*$pdlUrlPath.*"))
            .withHeader(HttpHeaders.Authorization, containing("Bearer"))
            .withHeader(HttpHeaders.ContentType, equalTo("application/json"))
            .withHeader(ProxiedHeader, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(PdlOk)
            )
    )

    return this
}

internal fun WireMockServer.pdlUrl(): String = baseUrl() + pdlUrlPath
