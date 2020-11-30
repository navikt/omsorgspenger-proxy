package no.nav.omsorgspenger

import com.auth0.jwt.interfaces.Claim
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.auth.*
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString

internal object Auth {
    /**
     * Om tokenet er scopet til 'omsorgspenger-proxy' vil tjenesten
     * veksle tokens iht. hva den bakomforliggende tjenesten trenger.
     *
     * Om tokenet er scopet til en annen client i Azure vil den kun sende
     * videre Azure-tokenet som ble sendt inn til 'omsorgspenger-proxy'
     * til den bakomforliggende tjenesten.
     */
    private const val AzureAnyScopeAlias = "azure_any_scoped"

    /**
     * Tokenet må være scopet til 'omsorgspenger-proxy'
     * Vil feile om den er scopet til andre clienter i Azure.
     */
    private const val AzureProxyScopedAlias = "azure_proxy_scoped"

    internal fun Map<Issuer,Set<ClaimRule>>.azureProxyScoped() = filterKeys {
        it.alias() == AzureProxyScopedAlias
    }.also { require(it.size == 1) }.allIssuers()

    internal fun Map<Issuer,Set<ClaimRule>>.azureAnyScoped() = filterKeys {
        it.alias() == AzureAnyScopeAlias
    }.also { require(it.size == 1) }.allIssuers()

    internal fun ApplicationConfig.omsorgspengerProxyIssuers() = issuers().withAdditionalClaimRules(mapOf(
        AzureAnyScopeAlias to setOf(AzureAnyScopedClaimRule(
            omsorgspengerProxyClientId = getRequiredString("nav.auth.azure_app_client_id", secret = false),
            authorizedClientIds = getRequiredList("nav.auth.azure_app_authorized_client_ids", secret = false, builder = {it}).toSet()
        )
    )))

    internal class AzureAnyScopedClaimRule(
        omsorgspengerProxyClientId: String,
        authorizedClientIds: Set<String>) : ClaimRule {
        private val enforceAudience = StandardClaimRules.Companion.EnforceAudienceEquals(
            requiredAudience = omsorgspengerProxyClientId
        )
        private val enforceAuthorizedClient = AzureClaimRules.Companion.EnforceAuthorizedClient(
            authorizedClients = authorizedClientIds
        )
        private val notProxyScoped = Successful("not_proxy_scoped", true)

        override fun enforce(claims: Map<String, Claim>): EnforcementOutcome {
            return when (enforceAudience.enforce(claims)) {
                is Successful -> enforceAuthorizedClient.enforce(claims)
                else -> notProxyScoped
            }
        }
    }
}

internal fun ApplicationCall.erScopetTilOmsorgspengerProxy(omsorgspengerProxyClientId: String) =
    principal<JWTPrincipal>()!!.payload.audience.contains(omsorgspengerProxyClientId)
