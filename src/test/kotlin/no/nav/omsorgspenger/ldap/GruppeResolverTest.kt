package no.nav.omsorgspenger.ldap

import no.nav.omsorgspenger.ldap.DefaultLdapGateway.Companion.gruppeResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.naming.directory.BasicAttribute

internal class GruppeResolverTest {

    @Test
    fun `Henter ut grupper på rett format`() {
        val memberOf = BasicAttribute("memberOf")
        memberOf.add("CN=0000-GA-k9-saksbehsandler,jojoa,neinei,CN=0000-GA-k9-beslutter")
        memberOf.add("foo,bar,CN=UkjentGruppe,CN=0000-GA-Annet-System")
        val grupper = memberOf.gruppeResolver()
        assertEquals(setOf("0000-GA-k9-saksbehsandler", "0000-GA-k9-beslutter", "UkjentGruppe", "0000-GA-Annet-System"), grupper)
    }
}
