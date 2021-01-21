package no.nav.omsorgspenger.testutils

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.jws.NaisSts

internal object AuthorizationHeaders {
    internal fun TestApplicationRequest.medOpenAm() {
        addHeader("X-Open-AM", "Bearer ${NaisSts.generateJwt(
            application = "foo",
            overridingClaims = mapOf(
                "sub" to "F1337",
                "tokenName" to "id_token"
            )
        )}")
    }
    internal fun TestApplicationRequest.medAzure(
        audience: String = azureAppClientId,
        clientId: String = "allowed-1") {
        addHeader(HttpHeaders.Authorization, "Bearer ${Azure.V2_0.generateJwt(
            clientId = clientId,
            audience = audience
        )}")
    }
}