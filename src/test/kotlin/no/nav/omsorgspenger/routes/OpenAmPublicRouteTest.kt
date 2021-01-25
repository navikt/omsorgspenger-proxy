package no.nav.omsorgspenger.routes

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.jws.NaisSts
import no.nav.omsorgspenger.testutils.TestApplicationExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert

@ExtendWith(TestApplicationExtension::class)
internal class OpenAmPublicRouteTest(
    private val testApplicationEngine: TestApplicationEngine) {
    private val keyPath = "/open-am/keys"

    @Test
    fun `Henter keys for open am`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, keyPath) {}.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                JSONAssert.assertEquals(NaisSts.getPublicJwk(), response.content, true)
            }
        }
    }
}