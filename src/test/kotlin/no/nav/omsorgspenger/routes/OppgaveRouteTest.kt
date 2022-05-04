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
internal class OppgaveRouteTest(
    private val testApplicationEngine: TestApplicationEngine
) {
    private val oppgaveUrl = "/oppgave/api/v1/blabla?testparam=any"

    @Test
    fun `ingen token gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, oppgaveUrl) {}.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    @Test
    fun `GET - token utstedt til oms-proxy proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, oppgaveUrl) {
                medAzure(clientId = "allowed-1")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("ProxiedHeader", "anything")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThat(response.headers[HttpHeaders.ContentType]).isEqualTo("application/json")
            }
        }
    }

    @Test
    fun `POST - token utstedt til oms-proxy proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, oppgaveUrl) {
                medAzure(clientId = "allowed-2")
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
    fun `PATCH - token utstedt til oms-proxy proxyer request`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Patch, "/oppgave/patch-request") {
                medAzure(clientId = "allowed-2")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("ProxiedHeader", "anything")
                setBody("{}")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
                assertThat(response.headers["X-Test-Header"]).isEqualTo("Er-Satt")
            }
        }
    }

    @Test
    fun `token scopet til annen tjeneste gir 403`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, oppgaveUrl) {
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
            handleRequest(HttpMethod.Get, oppgaveUrl) {
                medAzure(clientId = "not-allowed")
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader("ProxiedHeader", "anything")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }
}
