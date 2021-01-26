package no.nav.omsorgspenger.testutils

import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.engine.stop
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getNaisStsTokenUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getNaisStsWellKnownUrl
import no.nav.omsorgspenger.ApplicationContext
import no.nav.omsorgspenger.app
import no.nav.omsorgspenger.testutils.mocks.MockedLdapGateway
import no.nav.omsorgspenger.testutils.mocks.dokarkivproxyUrl
import no.nav.omsorgspenger.testutils.mocks.oppgaveUrl
import no.nav.omsorgspenger.testutils.mocks.pdlUrl
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.util.concurrent.TimeUnit

internal class TestApplicationExtension : ParameterResolver {
    @KtorExperimentalAPI
    internal companion object {
        private val mockedEnvironment = MockedEnvironment().start()
        private val applicationContext = ApplicationContext.Builder(
            ldapGateway = MockedLdapGateway(),
            env = mapOf(
                "username" to "test_username",
                "password" to "test_pw",
                "PDL_BASE_URL" to mockedEnvironment.wireMockServer.pdlUrl(),
                "OPPGAVE_BASE_URL" to mockedEnvironment.wireMockServer.oppgaveUrl(),
                "STS_TOKEN_URL" to mockedEnvironment.wireMockServer.getNaisStsTokenUrl(),
                "DOKARKIV_PROXY_BASE_URL" to mockedEnvironment.wireMockServer.dokarkivproxyUrl(),
                "AZURE_APP_WELL_KNOWN_URL" to mockedEnvironment.wireMockServer.getAzureV2WellKnownUrl(),
                "AZURE_APP_CLIENT_ID" to "omsorgspenger-proxy",
                "AZURE_APP_AUTHORIZED_CLIENT_IDS" to "allowed-1,allowed-2",
                "OPEN_AM_WELL_KNOWN_URL" to mockedEnvironment.wireMockServer.getNaisStsWellKnownUrl()
            )
        ).build()

        @KtorExperimentalAPI
        internal val testApplicationEngine = TestApplicationEngine(
            environment = createTestEnvironment {
                config = HoconApplicationConfig(ConfigFactory.load().withoutPath("ktor.application.modules"))
                module { app(applicationContext) }
            }
        )

        init {
            testApplicationEngine.start(wait = true)
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    testApplicationEngine.stop(10, 60, TimeUnit.SECONDS)
                    mockedEnvironment.stop()
                }
            )
        }
    }

    private val støttedeParametre = listOf(
        TestApplicationEngine::class.java,
        WireMockServer::class.java
    )

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return støttedeParametre.contains(parameterContext.parameter.type)
    }

    @KtorExperimentalAPI
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return when (parameterContext.parameter.type) {
            TestApplicationEngine::class.java -> testApplicationEngine
            else -> mockedEnvironment.wireMockServer
        }
    }
}