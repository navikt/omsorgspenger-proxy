package no.nav.omsorgspenger.routes

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medAzure
import no.nav.omsorgspenger.testutils.AuthorizationHeaders.medOpenAm
import no.nav.omsorgspenger.testutils.TestApplicationExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

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
    fun `Gyldig Azure og OpenAM token`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, memberOfPath) {
                medAzure()
                medOpenAm()
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }
        }
    }


}