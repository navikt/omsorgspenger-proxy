package no.nav.omsorgspenger.ldap

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

internal class LdapService(
    private val ldapGateway: LdapGateway) {

    private val cache : Cache<String, Set<String>> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(15))
        .maximumSize(100)
        .build()

    internal fun hentGrupper(navIdent: String) : Set<String> {
        return cache.getIfPresent(navIdent) ?: ldapGateway.hentGrupper(navIdent).also { grupper ->
            cache.put(navIdent, grupper)
        }
    }
}