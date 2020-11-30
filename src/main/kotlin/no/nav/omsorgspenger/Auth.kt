package no.nav.omsorgspenger

import no.nav.helse.dusseldorf.ktor.auth.ClaimRule
import no.nav.helse.dusseldorf.ktor.auth.Issuer
import no.nav.helse.dusseldorf.ktor.auth.allIssuers

internal fun Map<Issuer,Set<ClaimRule>>.azureProxyScoped() = filterKeys {
    it.alias() == "azure_proxy_scoped"
}.also { require(it.size == 1) }.allIssuers()
internal fun Map<Issuer,Set<ClaimRule>>.azureAnyScoped() = filterKeys {
    it.alias() == "azure_any_scoped"
}.also { require(it.size == 1) }.allIssuers()
