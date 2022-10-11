package no.nav.omsorgspenger.ldap

import no.nav.omsorgspenger.config.Config
import org.slf4j.LoggerFactory
import java.util.*
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

internal interface LdapGateway {
    fun hentGrupper(navIdent: String): Set<String>
}

internal class DefaultLdapGateway(
    ldapConfig: Config.Ldap
) : LdapGateway {

    private val ldapEnvironment = Hashtable(
        mapOf(
            Context.INITIAL_CONTEXT_FACTORY to "com.sun.jndi.ldap.LdapCtxFactory",
            Context.PROVIDER_URL to ldapConfig.url,
            Context.SECURITY_AUTHENTICATION to "simple",
            Context.SECURITY_PRINCIPAL to ldapConfig.username,
            Context.SECURITY_CREDENTIALS to ldapConfig.password
        )
    )

    private val searchBase = ldapConfig.searchBase

    private val searchControls = SearchControls().also {
        it.searchScope = SearchControls.SUBTREE_SCOPE
    }

    private fun search(navIdent: String) = ldapContext().search(searchBase, "(cn=$navIdent)", searchControls).searchResult()

    override fun hentGrupper(navIdent: String): Set<String> {
        val searchResult = search(navIdent) ?: return emptySet()
        val memberOf = searchResult.attribute(MemberOf) ?: return emptySet()
        return memberOf.gruppeResolver()
    }

    private fun ldapContext(): LdapContext = InitialLdapContext(ldapEnvironment, null)

    internal companion object {
        private const val MemberOf = "memberOf"
        private fun NamingEnumeration<SearchResult>.searchResult(): SearchResult? {
            return if (!hasMoreElements()) {
                logger.warn("Ingen SearchResult")
                null
            } else { nextElement() }
        }
        private fun SearchResult.attribute(key: String): Attribute? = attributes[key].also { attribute ->
            if (attribute == null) {
                logger.warn("Inneholder ikke Attribute[$key]")
            }
        }
        private val logger = LoggerFactory.getLogger(LdapGateway::class.java)

        internal fun Attribute.gruppeResolver() = all
            .iterator()
            .asSequence()
            .map { it as String }
            .map { it.split(",") }
            .flatten()
            .filter { it.startsWith("CN") }
            .map { it.substringAfter("=") }
            .toSet()
    }
}
