package no.nav.omsorgspenger

internal class ApplicationContext(
    internal val env: Map<String, String>
) {

    internal class Builder(
        internal var env: Map<String, String>? = null
    ) {

        internal fun build(): ApplicationContext {
            val benyttetEnv = env ?: System.getenv()

            return ApplicationContext(
                env = benyttetEnv
            )
        }
    }
}
