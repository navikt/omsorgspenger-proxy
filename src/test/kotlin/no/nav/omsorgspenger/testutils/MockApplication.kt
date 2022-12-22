package no.nav.omsorgspenger.testutils

import io.ktor.server.config.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.testsupport.wiremock.getAzureV2WellKnownUrl
import no.nav.helse.dusseldorf.testsupport.wiremock.getNaisStsTokenUrl
import no.nav.omsorgspenger.testutils.mocks.infotrygdGrunnlagPaaroerendeSykdomUrl
import no.nav.omsorgspenger.testutils.mocks.k9SakUrl
import no.nav.omsorgspenger.testutils.mocks.sakUrl

internal fun TestApplicationBuilder.mockApp(
    mockedEnvironment: MockedEnvironment = MockedEnvironment().start()
) {
    val env = mapOf(
        "username" to "test_username",
        "password" to "test_pw",
        "STS_TOKEN_URL" to mockedEnvironment.wireMockServer.getNaisStsTokenUrl(),
        "AZURE_APP_WELL_KNOWN_URL" to mockedEnvironment.wireMockServer.getAzureV2WellKnownUrl(),
        "AZURE_APP_CLIENT_ID" to "omsorgspenger-proxy",
        "AZURE_APP_PRE_AUTHORIZED_APPS" to """[{"clientId":"allowed-1"},{"clientId":"allowed-2"}]""",
        "K9_SAK_BASE_URL" to mockedEnvironment.wireMockServer.k9SakUrl(),
        "SAK_BASE_URL" to mockedEnvironment.wireMockServer.sakUrl(),
        "INFOTRYGD_GRUNNLAG_PAAROERENDE_SYKDOM_BASE_URL" to mockedEnvironment.wireMockServer.infotrygdGrunnlagPaaroerendeSykdomUrl(),
        "K9_INFOTRYGD_BASE_URL" to mockedEnvironment.wireMockServer.infotrygdGrunnlagPaaroerendeSykdomUrl(),
    )

    return application { app(env) }
}
