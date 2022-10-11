package no.nav.omsorgspenger

import no.nav.omsorgspenger.config.Config
import no.nav.omsorgspenger.ldap.DefaultLdapGateway
import no.nav.omsorgspenger.ldap.LdapGateway

internal class ApplicationContext(
    internal val env: Map<String, String>,
    internal val ldapGateway: LdapGateway
) {

    internal class Builder(
        internal var env: Map<String, String>? = null,
        internal var ldapGateway: LdapGateway? = null
    ) {

        internal fun build(): ApplicationContext {
            val benyttetEnv = env ?: System.getenv()

            return ApplicationContext(
                env = benyttetEnv,
                ldapGateway = ldapGateway ?: DefaultLdapGateway(
                    ldapConfig = Config.Ldap(benyttetEnv)
                )
            )
        }
    }
}
