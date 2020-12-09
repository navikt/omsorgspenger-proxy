package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.HttpHeaders

private const val dokarkivproxyPath = "/dokarkivproxy-mock"

private fun MappingBuilder.dokarkivproxyMapping() = this
    .withHeader(HttpHeaders.Authorization, containing("Bearer "))
    .withHeader(ProxiedHeader, AnythingPattern())
    .willReturn(
        aResponse()
            .withBody("{}")
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
    )

private fun WireMockServer.stubDokarkivproxyPut(): WireMockServer {
    stubFor(
        WireMock.put(urlPathMatching(".*$dokarkivproxyPath.*"))
            .withHeader(HttpHeaders.ContentType, WireMock.equalTo("application/json"))
            .dokarkivproxyMapping()
    )
    return this
}

internal fun WireMockServer.dokarkivproxyUrl(): String = baseUrl() + dokarkivproxyPath

internal fun WireMockServer.stubDokarkivproxy() = this
    .stubDokarkivproxyPut()
