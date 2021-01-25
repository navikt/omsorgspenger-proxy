package no.nav.omsorgspenger.ldap

import no.nav.omsorgspenger.ldap.DefaultLdapGateway.Companion.gruppeResolver
import org.junit.jupiter.api.Test
import javax.naming.directory.BasicAttribute
import kotlin.test.assertEquals

internal class GruppeResolverTest {

    @Test
    fun `Henter ut grupper på rett format`() {
        val memberOf = BasicAttribute("memberOf")
        memberOf.add("CN=0000-GA-k9-saksbehsandler,jojoa,neinei,CN=0000-GA-k9-beslutter")
        val grupper = memberOf.gruppeResolver()
        assertEquals(setOf("0000-GA-k9-saksbehsandler", "0000-GA-k9-beslutter"), grupper)
    }
}