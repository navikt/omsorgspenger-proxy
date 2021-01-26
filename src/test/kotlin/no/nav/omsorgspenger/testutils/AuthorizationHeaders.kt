package no.nav.omsorgspenger.testutils

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.jws.NaisSts

internal object AuthorizationHeaders {
    internal fun TestApplicationRequest.medOpenAm(navIdent: String = "person1") {
        addHeader("X-Open-AM", "Bearer ${NaisSts.generateJwt(
            application = "foo",
            overridingClaims = mapOf(
                "sub" to navIdent,
                "tokenName" to "id_token"
            )
        )}")
    }
    internal fun TestApplicationRequest.medAzure(
        audience: String = "omsorgspenger-proxy",
        clientId: String = "allowed-1") {
        addHeader(HttpHeaders.Authorization, "Bearer ${Azure.V2_0.generateJwt(
            clientId = clientId,
            audience = audience
        )}")
    }
}