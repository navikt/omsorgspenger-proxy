package no.nav.omsorgspenger

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.omsorgspenger.testutils.MockedEnvironment
import no.nav.omsorgspenger.testutils.mockApp
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HealthTest {

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
    fun `isready gir 200`() = testApplication {
        mockApp(mockedEnvironment)
        client.get("/isready").apply {
            Assertions.assertEquals("READY", this.bodyAsText())
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
        }
    }

    @Test
    fun `isalive gir 200`() = testApplication {
        mockApp(mockedEnvironment)
        client.get("/isalive").apply {
            Assertions.assertEquals("ALIVE", this.bodyAsText())
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
        }
    }
}
