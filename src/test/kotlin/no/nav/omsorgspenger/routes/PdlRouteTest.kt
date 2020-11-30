package no.nav.omsorgspenger.routes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.omsorgspenger.testutils.TestApplicationExtension
import no.nav.omsorgspenger.testutils.azureAppClientId
import no.nav.omsorgspenger.testutils.mocks.ProxiedHeader
import no.nav.omsorgspenger.testutils.mocks.likeHeadersBody
import no.nav.omsorgspenger.testutils.mocks.ulikeHeadersBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApplicationExtension::class)
internal class PdlRouteTest(
    private val testApplicationEngine: TestApplicationEngine
) {
    private val pdlUrl = "/pdl/graphql/blabla"

    @Test
    fun `ingen token gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, pdlUrl) {}.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    @Test
    fun `token utstedt til oms-proxy proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, pdlUrl) {
                addHeader(HttpHeaders.Authorization, "Bearer ${azureIssuerToken()}")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(likeHeadersBody)
            }
        }
    }

    @Test
    fun `token med annen audience propagerer auth header og proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, pdlUrl) {
                addHeader(HttpHeaders.Authorization, "Bearer ${azureIssuerToken("ikke-omsorgspenger-proxy")}")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(ulikeHeadersBody)
            }
        }
    }
}

internal fun azureIssuerToken(audience: String = azureAppClientId) = Azure.V2_0.generateJwt(
    clientId = "any",
    audience = audience
)
