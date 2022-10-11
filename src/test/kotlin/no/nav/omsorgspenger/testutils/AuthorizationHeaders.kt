package no.nav.omsorgspenger.testutils

import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.dusseldorf.testsupport.jws.NaisSts

internal object AuthorizationHeaders {
    internal fun HttpRequestBuilder.medOpenAm(navIdent: String = "person1") {
        header(
            "X-Open-AM", "Bearer ${
                NaisSts.generateJwt(
                    application = "foo",
                    overridingClaims = mapOf(
                        "sub" to navIdent,
                        "tokenName" to "id_token"
                    )
                )
            }"
        )
    }

    internal fun HttpRequestBuilder.medAzure(
        audience: String = "omsorgspenger-proxy",
        clientId: String = "allowed-1"
    ) {
        header(
            HttpHeaders.Authorization, "Bearer ${
                Azure.V2_0.generateJwt(
                    clientId = clientId,
                    audience = audience
                )
            }"
        )
    }
}