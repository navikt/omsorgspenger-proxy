package no.nav.omsorgspenger.config

internal object Config {

    internal class ServiceUser(env: Map<String, String>) {
        internal val username = env.getOrFail("username")
        internal val password = env.getOrFail("password")
    }

    internal class K9Sak(env: Map<String, String>) {
        internal val url = env.getOrFail("K9_SAK_BASE_URL")
    }

    internal class STS(env: Map<String, String>) {
        internal val url = env.getOrFail("STS_TOKEN_URL")
    }

    internal fun Map<String, String>.getOrFail(key: String) = getOrElse(key) {
        throw IllegalStateException("Mangler Environment variable $key")
    }
}
