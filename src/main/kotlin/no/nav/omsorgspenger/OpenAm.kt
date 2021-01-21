package no.nav.omsorgspenger

import io.ktor.application.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.auth.EnforceEqualsOrContains
import no.nav.helse.dusseldorf.ktor.auth.Issuer
import no.nav.helse.dusseldorf.ktor.auth.JwtVerifier
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import org.json.JSONObject
import java.net.URI

internal class OpenAm(
    private val wellKnownUri: URI) {

    private val issuerOgJwksUri = runBlocking {
        val json = JSONObject(wellKnownUri.toString().httpGet().readTextOrThrow().second)
        requireNotNull(json.getString("issuer")) to URI(requireNotNull(json.getString("jwks_uri")))
    }

    private val jwtVerifier = JwtVerifier(
        issuer = Issuer(
            alias = "open_am",
            issuer = issuerOgJwksUri.first,
            jwksUri = issuerOgJwksUri.second,
            audience = null
        ),
        additionalClaimRules = setOf(EnforceEqualsOrContains(
            defaultClaimName = "tokenName",
            expected = "id_token"
        ))
    )

    internal fun verifisertHeaderValue(call: ApplicationCall) : String {
        require(call.harOpenAmToken()) {
            "Requesten inneholder ikke headeren $HeaderNavn"
        }
        val headerValue = call.request.headers[HeaderNavn]!!
        val jwt = headerValue.removePrefix("Bearer ")

        jwtVerifier.verify(jwt).also { verified -> if (!verified) {
            throw Throwblem(problemDetails)
        }}

        return headerValue
    }

    internal companion object {
        private const val HeaderNavn = "X-Open-AM"
        private val problemDetails = DefaultProblemDetails(
            title = "unauthorized",
            status = 403,
            detail = "Requesten inneholder ikke tilstrekkelige tilganger."
        )
        internal fun ApplicationCall.harOpenAmToken() = request.headers.contains(HeaderNavn)
    }
}