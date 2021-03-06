package no.nav.omsorgspenger.testutils.mocks

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import no.nav.omsorgspenger.NavConsumerToken

private const val pdlUrlPath = "/pdl-mock"

internal const val tokenTransformerMatcher = " tokenTransformerMatcher"
internal const val likeHeadersBody = "like"
internal const val ulikeHeadersBody = "ulike"
internal const val ProxiedHeader = "Proxiedheader"

internal fun WireMockServer.stubPdl(): WireMockServer {
    WireMock.stubFor(
        WireMock.post(WireMock.urlPathMatching(".*$pdlUrlPath.*"))
            .withHeader(io.ktor.http.HttpHeaders.Authorization, containing("Bearer"))
            .withHeader(io.ktor.http.HttpHeaders.ContentType, equalTo("application/json"))
            .withHeader(ProxiedHeader, AnythingPattern())
            .willReturn(
                WireMock.aResponse()
                    .withTransformers(tokenTransformerMatcher)
            )
    )

    return this
}

internal class TokenResponseTransformer(
    private val name: String
) : ResponseTransformer() {

    override fun getName() = name
    override fun applyGlobally() = false

    override fun transform(
        request: Request,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val authorizationHeader = request.getHeader(io.ktor.http.HttpHeaders.Authorization)
        val navConsumerTokenHeader = request.getHeader(NavConsumerToken)

        val responseBody = if (authorizationHeader == navConsumerTokenHeader)
            likeHeadersBody
        else
            ulikeHeadersBody

        return Response.Builder.like(response)
            .status(200)
            .headers(HttpHeaders(HttpHeader.httpHeader(io.ktor.http.HttpHeaders.ContentType, "application/json; charset=UTF-8")))
            .body(responseBody)
            .build()
    }
}

internal fun WireMockServer.pdlUrl(): String = baseUrl() + pdlUrlPath
