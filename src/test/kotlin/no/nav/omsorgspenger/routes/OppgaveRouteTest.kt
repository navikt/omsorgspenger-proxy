package no.nav.omsorgspenger.routes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import no.nav.omsorgspenger.testutils.TestApplicationExtension
import no.nav.omsorgspenger.testutils.mocks.ProxiedHeader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApplicationExtension::class)
internal class OppgaveRouteTest(
    private val testApplicationEngine: TestApplicationEngine
) {
    @Test
    internal fun `ingen token gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/oppgave") {}.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    @Test
    internal fun `GET - token utstedt til oms-proxy proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/oppgave") {
                addHeader(HttpHeaders.Authorization, "Bearer ${azureIssuerToken()}")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.headers[HttpHeaders.ContentType]).isEqualTo("application/json")
            }
        }
    }

    @Test
    internal fun `POST - token utstedt til oms-proxy proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, "/oppgave") {
                addHeader(HttpHeaders.Authorization, "Bearer ${azureIssuerToken()}")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.headers[HttpHeaders.ContentType]).isEqualTo("application/json")
            }
        }
    }

    @Test
    internal fun `token scopet til annen tjeneste gir 403`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/oppgave") {
                addHeader(HttpHeaders.Authorization, "Bearer ${azureIssuerToken(audience = "ikke-oms-proxy")}")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(ProxiedHeader, "anything")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }
}
