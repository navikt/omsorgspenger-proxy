package no.nav.omsorgspenger.routes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medAzure
import no.nav.omsorgspenger.testutils.TestApplicationExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApplicationExtension::class)
internal class DokarkivproxyRouteTest(
    private val testApplicationEngine: TestApplicationEngine
) {
    private val dokarkivproxyUrl = "/dokarkivproxy-mock/test"

    @Test
    fun `ingen token gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Put, dokarkivproxyUrl) {}.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    @Test
    fun `PUT - token utstedt til oms-proxy proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Put, dokarkivproxyUrl) {
                medAzure()
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("ProxiedHeader", "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.headers[HttpHeaders.ContentType]).isEqualTo("application/json")
            }
        }
    }

    @Test
    fun `token scopet til annen tjeneste gir 403`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Put, dokarkivproxyUrl) {
                medAzure(audience = "ikke-oms-proxy")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("ProxiedHeader", "anything")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }

    @Test
    fun `token fra en ikke authorized client gir 403`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Put, dokarkivproxyUrl) {
                medAzure(clientId = "not-allowed")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("ProxiedHeader", "anything")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }
}
