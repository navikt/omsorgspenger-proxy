package no.nav.omsorgspenger.routes

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medAzure
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medOpenAm
import no.nav.omsorgspenger.testutils.TestApplicationExtension
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert

@ExtendWith(TestApplicationExtension::class)
internal class ActiveDirectoryRouteTest(
    private val testApplicationEngine: TestApplicationEngine) {
    private val memberOfPath = "/active-directory/me/memberOf"

    @Test
    fun `ingen token gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, memberOfPath) {}.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    @Test
    fun `kun  open ap token gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, memberOfPath) {
                medOpenAm()
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    @Test
    fun `azure token scopet feil`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, memberOfPath) {
                medAzure("ikke-proxy")
                medOpenAm()
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.Forbidden)
            }
        }
    }

    @Test
    fun `Gyldig tokens, medlem av en gruppe`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, memberOfPath) {
                medAzure()
                medOpenAm(navIdent = "person2")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
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
                JSONAssert.assertEquals(forventet, response.content, true)
            }
        }
    }

    @Test
    fun `Gyldig tokens, medlem av to grupper`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, memberOfPath) {
                medAzure()
                medOpenAm()
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                @Language("JSON")
                val forventet = """
                {
                  "value": [{
                    "id": "gruppe1",
                    "displayName": "gruppe1"
                  }]
                }
                """.trimIndent()
                JSONAssert.assertEquals(forventet, response.content, true)
            }
        }
    }

    @Test
    fun `Gyldig tokens, ikke medlem av noen grupper`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, memberOfPath) {
                medAzure()
                medOpenAm(navIdent = "404")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                @Language("JSON")
                val forventet = """
                {
                  "value": []
                }
                """.trimIndent()
                JSONAssert.assertEquals(forventet, response.content, true)
            }
        }
    }
}