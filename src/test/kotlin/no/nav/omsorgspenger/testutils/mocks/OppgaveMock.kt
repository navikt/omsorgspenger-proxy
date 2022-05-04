package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import io.ktor.http.HttpHeaders

private const val oppgaveUrlPath = "/oppgave-mock"

private fun MappingBuilder.oppgaveMapping() = this
    .withHeader(HttpHeaders.Authorization, containing("Bearer "))
    .withHeader("ProxiedHeader", AnythingPattern())
    .withQueryParam("testparam", AnythingPattern())
    .willReturn(
        aResponse()
            .withBody("{}")
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
    )

private fun WireMockServer.stubOppgavePost(): WireMockServer {
    stubFor(
        WireMock.post(urlPathMatching(".*$oppgaveUrlPath.*"))
            .withHeader(HttpHeaders.ContentType, WireMock.equalTo("application/json"))
            .oppgaveMapping()
    )
    return this
}

private fun WireMockServer.stubOppgaveGet(): WireMockServer {
    stubFor(
        WireMock.get(urlPathMatching(".*$oppgaveUrlPath.*"))
            .oppgaveMapping()
    )
    return this
}

private fun WireMockServer.stubOppgavePatch(): WireMockServer {
    stubFor(
        WireMock.patch(urlPathMatching(".*$oppgaveUrlPath/patch-request"))
            .withHeader(HttpHeaders.Authorization, containing("Bearer "))
            .withRequestBody(EqualToPattern("{}"))
            .willReturn(aResponse()
                .withStatus(204)
                .withHeader("X-Test-Header", "Er-Satt")
            )
    )
    return this
}


internal fun WireMockServer.oppgaveUrl(): String = baseUrl() + oppgaveUrlPath

internal fun WireMockServer.stubOppgave() = this
    .stubOppgaveGet()
    .stubOppgavePost()
    .stubOppgavePatch()
