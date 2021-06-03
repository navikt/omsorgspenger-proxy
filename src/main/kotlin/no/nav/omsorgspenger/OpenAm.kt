package no.nav.omsorgspenger

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import no.nav.helse.dusseldorf.ktor.auth.EnforceEqualsOrContains
import no.nav.helse.dusseldorf.ktor.auth.Issuer
import no.nav.helse.dusseldorf.ktor.auth.JwtVerifier
import no.nav.helse.dusseldorf.ktor.core.DefaultProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.omsorgspenger.Auth.discover
import no.nav.omsorgspenger.config.Config

internal class OpenAm(
    openAmConfig: Config.OpenAM) {
    private val issuerOgJwksUri = openAmConfig.wellKnownUri.discover()
    internal val jwksUri = issuerOgJwksUri.second

    private val jwtVerifier = JwtVerifier(
        issuer = Issuer(
            alias = "open_am",
            issuer = issuerOgJwksUri.first,
            jwksUri = issuerOgJwksUri.second,
            audience = null
        ),
        additionalClaimRules = setOf(
            EnforceEqualsOrContains(
                defaultClaimName = "tokenName",
                expected = "id_token"
            )
        )
    )

    private fun verifisert(call: ApplicationCall) : Pair<String, DecodedJWT> {
        require(call.harOpenAmToken()) {
            "Requesten inneholder ikke headeren $HeaderNavn"
        }
        val headerValue = call.request.headers[HeaderNavn]!!
        val jwt = headerValue.removePrefix("Bearer ")

        jwtVerifier.verify(jwt).also { verified -> if (!verified) {
            throw Throwblem(problemDetails)
        }}

        return headerValue to JWT.decode(jwt)
    }

    internal fun verifisertHeaderValue(call: ApplicationCall) = verifisert(call).first

    internal fun verifisertUserInfo(call: ApplicationCall) = UserInfo(
        navIdent = verifisert(call).second.subject
    )

    internal data class UserInfo(
        internal val navIdent: String
    )

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