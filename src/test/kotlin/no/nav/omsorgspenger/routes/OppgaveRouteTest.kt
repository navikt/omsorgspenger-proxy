package no.nav.omsorgspenger.routes

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medAzure
import no.nav.omsorgspenger.testutils.MockedEnvironment
import no.nav.omsorgspenger.testutils.mockApp
import no.nav.omsorgspenger.testutils.mocks.ProxiedHeader
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OppgaveRouteTest {
    private val oppgaveUrl = "/oppgave/api/v1/blabla?testparam=any"
    private lateinit var mockedEnvironment: MockedEnvironment

    @BeforeAll
    fun setup() {
        mockedEnvironment = MockedEnvironment().start()
    }

    @AfterAll
    fun tearDown() {
        mockedEnvironment.stop()
    }

    @Test
    fun `ingen token gir 401`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(oppgaveUrl).apply {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, this.status)
        }
    }

    @Test
    fun `GET - token utstedt til oms-proxy proxyer request`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(oppgaveUrl) {
            medAzure(clientId = "allowed-1")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
            Assertions.assertEquals("application/json", this.headers[HttpHeaders.ContentType])
        }
    }

    @Test
    fun `POST - token utstedt til oms-proxy proxyer request`() = testApplication {
        mockApp(mockedEnvironment)
        client.post(oppgaveUrl) {
            medAzure(clientId = "allowed-2")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
            setBody("{}")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
            Assertions.assertEquals("application/json", this.headers[HttpHeaders.ContentType])
        }
    }

    @Test
    fun `PATCH - token utstedt til oms-proxy proxyer request`() = testApplication {
        mockApp(mockedEnvironment)
        client.patch("/oppgave/patch-request") {
            medAzure(clientId = "allowed-2")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
            setBody("{}")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.NoContent, this.status)
            Assertions.assertEquals("Er-Satt", this.headers["X-Test-Header"])
        }
    }

    @Test
    fun `token scopet til annen tjeneste gir 403`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(oppgaveUrl) {
            medAzure(audience = "ikke-oms-proxy")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.Forbidden, this.status)
        }
    }

    @Test
    fun `token fra en ikke authorized client gir 403`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(oppgaveUrl) {
            medAzure(clientId = "not-allowed")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.Forbidden, this.status)
        }
    }
}
