package no.nav.omsorgspenger.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medAzure
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medOpenAm
import no.nav.omsorgspenger.testutils.MockedEnvironment
import no.nav.omsorgspenger.testutils.mockApp
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.skyscreamer.jsonassert.JSONAssert

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActiveDirectoryRouteTest {

    private val memberOfPath = "/active-directory/me/memberOf"
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
        client.get(memberOfPath).apply {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, this.status)
        }
    }

    @Test
    fun `kun  open ap token gir 401`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(memberOfPath) {
            medOpenAm()
        }.apply {
            Assertions.assertEquals(HttpStatusCode.Unauthorized, this.status)
        }
    }

    @Test
    fun `azure token scopet feil`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(memberOfPath) {
            medAzure("ikke-proxy")
            medOpenAm()
        }.apply {
            Assertions.assertEquals(HttpStatusCode.Forbidden, this.status)
        }
    }

    @Test
    fun `Gyldig tokens, medlem av en gruppe`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(memberOfPath) {
            medAzure()
            medOpenAm(navIdent = "person2")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
            @Language("JSON")
            val forventet = """
                {
                  "value": [{
                    "id": "gruppe1",
                    "displayName": "gruppe1"
                  }, {
                    "id": "gruppe2",
                    "displayName": "gruppe2"
                  }]
                }
                """.trimIndent()
            JSONAssert.assertEquals(forventet, this.bodyAsText(), true)
        }
    }

    @Test
    fun `Gyldig tokens, medlem av to grupper`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(memberOfPath) {
            medAzure()
            medOpenAm()
        }.apply {
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
            @Language("JSON")
            val forventet = """
                {
                  "value": [{
                    "id": "gruppe1",
                    "displayName": "gruppe1"
                  }]
                }
                """.trimIndent()
            JSONAssert.assertEquals(forventet, this.bodyAsText(), true)
        }
    }

    @Test
    fun `Gyldig tokens, ikke medlem av noen grupper`() = testApplication {
        mockApp(mockedEnvironment)
        client.get(memberOfPath) {
            medAzure()
            medOpenAm(navIdent = "404")
        }.apply {
            Assertions.assertEquals(HttpStatusCode.OK, this.status)
            @Language("JSON")
            val forventet = """
                {
                  "value": []
                }
                """.trimIndent()
            JSONAssert.assertEquals(forventet, this.bodyAsText(), true)
        }
    }
}