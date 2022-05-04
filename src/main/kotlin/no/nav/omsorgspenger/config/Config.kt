package no.nav.omsorgspenger.config

import java.net.URI

internal object Config {

    internal class ServiceUser(env: Map<String, String>) {
        internal val username = env.getOrFail("username")
        internal val password = env.getOrFail("password")
    }

    internal class PDL(env: Map<String, String>) {
        internal val url = env.getOrFail("PDL_BASE_URL")
    }

    internal class SAF(env: Map<String, String>) {
        internal val url = env.getOrFail("SAF_BASE_URL")
    }

    internal class Oppgave(env: Map<String, String>) {
        internal val url = env.getOrFail("OPPGAVE_BASE_URL")
    }

    internal class K9Sak(env: Map<String, String>) {
        internal val url = env.getOrFail("K9_SAK_BASE_URL")
    }

    internal class STS(env: Map<String, String>) {
        internal val url = env.getOrFail("STS_TOKEN_URL")
    }

    internal class Dokarkivproxy(env: Map<String, String>) {
        internal val url = env.getOrFail("DOKARKIV_PROXY_BASE_URL")
    }

    internal class InfotrygdGrunnlagPaaroerendeSykdom(env: Map<String, String>) {
        internal val url = env.getOrFail("INFOTRYGD_GRUNNLAG_PAAROERENDE_SYKDOM_BASE_URL")
    }

    internal class OpenAM(env: Map<String, String>) {
        internal val wellKnownUri = URI(env.getOrFail("OPEN_AM_WELL_KNOWN_URL"))
    }

    internal class Ldap(env: Map<String, String>) {
        internal val url = env.getOrFail("LDAP_URL")
        internal val username = env.getOrFail("LDAP_USERNAME")
        internal val password = env.getOrFail("LDAP_PASSWORD")
        internal val searchBase = env.getOrFail("LDAP_SEARCH_BASE")
    }

    internal fun Map<String, String>.getOrFail(key: String) = getOrElse(key) {
        throw IllegalStateException("Mangler Environment variable $key")
    }
}

