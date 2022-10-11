package no.nav.omsorgspenger.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medAzure
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medOpenAm
import no.nav.omsorgspenger.testutils.MockedEnvironment
import no.nav.omsorgspenger.testutils.mockApp
import no.nav.omsorgspenger.testutils.mocks.PdlOk
import no.nav.omsorgspenger.testutils.mocks.ProxiedHeader
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PdlRouteTest {
    private val pdlUrl = "/pdl/graphql/blabla"
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
        client.post(pdlUrl).apply {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, this.status)
        }
    }

    @Test
    fun `token utstedt til oms-proxy med open-am-token proxyer request`() = testApplication {
        mockApp(mockedEnvironment)
        client.post(pdlUrl) {
            medAzure(clientId = "allowed-1")
            medOpenAm()
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
            setBody("{}")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
            Assertions.assertEquals(PdlOk, this.bodyAsText())
        }
    }

    @Test
    fun `token utstedt til oms-proxy uten open-am-token feiler`() = testApplication {
        mockApp(mockedEnvironment)
        client.post(pdlUrl) {
            medAzure(clientId = "allowed-1")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
            setBody("{}")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.Forbidden, this.status)
        }
    }

    @Test
    fun `token utstedt til oms-proxy men ikke allowed client feiler`() = testApplication {
        mockApp(mockedEnvironment)
        client.post(pdlUrl) {
            medAzure(clientId = "not-allowed")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
            setBody("{}")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.Forbidden, this.status)
        }
    }

    @Test
    fun `token med annen audience feiler`() = testApplication {
        mockApp(mockedEnvironment)
        client.post(pdlUrl) {
            medAzure(audience = "ikke-omsorgspenger-proxy")
            header(HttpHeaders.ContentType, "application/json")
            header(ProxiedHeader, "anything")
            setBody("{}")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.Forbidden, this.status)
        }
    }
}