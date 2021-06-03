package no.nav.omsorgspenger

import com.auth0.jwt.interfaces.Claim
import kotlinx.coroutines.runBlocking
import no.nav.helse.dusseldorf.ktor.auth.*
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.httpGet
import no.nav.helse.dusseldorf.ktor.client.SimpleHttpClient.readTextOrThrow
import no.nav.omsorgspenger.config.Config.getOrFail
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.net.URI

internal object Auth {
    private val logger = LoggerFactory.getLogger(Auth::class.java)
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

    internal fun Map<String, String>.omsorgspengerProxyIssuers(): Map<Issuer, Set<ClaimRule>> {

        val enforceAuthorizedClient = AzureClaimRules.Companion.EnforceAuthorizedClient(
            authorizedClients = JSONArray(getOrFail("AZURE_APP_PRE_AUTHORIZED_APPS"))
              .map { it as JSONObject }
              .map { it.getString("clientId") }
              .toSet()
              .also { logger.info("AuthorizedClientIds=$it") }
        )

        val (azureIssuer, azureJwksUri) = URI(getOrFail("AZURE_APP_WELL_KNOWN_URL")).discover()

        val issuers = mapOf(
            AzureProxyScopedAlias to Issuer(
                alias = AzureProxyScopedAlias,
                issuer = azureIssuer,
                jwksUri = azureJwksUri,
                audience = getOrFail("AZURE_APP_CLIENT_ID")
            ),
            AzureAnyScopeAlias to Issuer(
                alias = AzureAnyScopeAlias,
                issuer = azureIssuer,
                jwksUri = azureJwksUri,
                audience = null
            )
        )

        return issuers.withAdditionalClaimRules(
            mapOf(
                AzureAnyScopeAlias to setOf(
                    AzureAnyScopedClaimRule(
                        omsorgspengerProxyClientId = getOrFail("AZURE_APP_CLIENT_ID"),
                        enforceAuthorizedClient = enforceAuthorizedClient
                    )
                ),
                AzureProxyScopedAlias to setOf(
                    enforceAuthorizedClient
                )
            )
        )
    }

    internal class AzureAnyScopedClaimRule(
        omsorgspengerProxyClientId: String,
        private val enforceAuthorizedClient: AzureClaimRules.Companion.EnforceAuthorizedClient) : ClaimRule {
        private val enforceAudience = StandardClaimRules.Companion.EnforceAudienceEquals(
            requiredAudience = omsorgspengerProxyClientId
        )

        private val notProxyScoped = Successful("not_proxy_scoped", true)

        override fun enforce(claims: Map<String, Claim>): EnforcementOutcome {
            return when (enforceAudience.enforce(claims)) {
                is Successful -> enforceAuthorizedClient.enforce(claims)
                else -> notProxyScoped
            }
        }
    }

    internal fun URI.discover(): Pair<String, URI> = this.toString().let { url -> runBlocking {
        val json = JSONObject(url.httpGet().readTextOrThrow().second)
        requireNotNull(json.getString("issuer")) to URI(requireNotNull(json.getString("jwks_uri")))
    }}
}