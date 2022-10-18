package no.nav.omsorgspenger.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.jws.NaisSts
import no.nav.omsorgspenger.testutils.MockedEnvironment
import no.nav.omsorgspenger.testutils.mockApp
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.skyscreamer.jsonassert.JSONAssert

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OpenAmPublicRouteTest {
    private val keyPath = "/open-am/keys"
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
    fun `Henter keys for open am`() = testApplication {
        mockApp()
        client.get(keyPath).apply {
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
            JSONAssert.assertEquals(NaisSts.getPublicJwk(), this.bodyAsText(), true)
        }
    }
}
