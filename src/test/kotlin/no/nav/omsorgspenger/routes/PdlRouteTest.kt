package no.nav.omsorgspenger.routes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medAzure
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medOpenAm
import no.nav.omsorgspenger.testutils.TestApplicationExtension
import no.nav.omsorgspenger.testutils.mocks.PdlOk
import no.nav.omsorgspenger.testutils.mocks.ProxiedHeader
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
    fun `token utstedt til oms-proxy med open-am-token proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, pdlUrl) {
                medAzure(clientId = "allowed-1")
                addHeader(HttpHeaders.ContentType, "application/json")
                medOpenAm()
                addHeader(ProxiedHeader, "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.content).isEqualTo(PdlOk)
            }
        }
    }

    @Test
    fun `token utstedt til oms-proxy uten open-am-token feiler`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, pdlUrl) {
                medAzure(clientId = "allowed-1")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }

    @Test
    fun `token utstedt til oms-proxy men ikke allowed client feiler`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, pdlUrl) {
                medAzure(clientId = "not-allowed")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }


    @Test
    fun `token med annen audience feiler`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, pdlUrl) {
                medAzure(audience = "ikke-omsorgspenger-proxy")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }
}