package no.nav.omsorgspenger.testutils.mocks

import no.nav.omsorgspenger.ldap.LdapGateway

internal class MockedLdapGateway : LdapGateway {
    override fun hentGrupper(navIdent: String): Set<String> {
        return when (navIdent) {
            "person1" -> setOf("gruppe1")
            "person2" -> setOf("gruppe1", "gruppe2")
            else -> emptySet()
        }
    }
}