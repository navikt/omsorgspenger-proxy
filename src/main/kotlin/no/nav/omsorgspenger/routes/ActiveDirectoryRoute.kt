package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspenger.OpenAm
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.ActiveDirectoryRoute")

internal fun Route.ActiveDirectoryRoute(
    openAm: OpenAm) {

    get("/active-directory/groups") {
        val userInfo = openAm.verifisertUserInfo(call)

        call.respondText(
            status = HttpStatusCode.OK,
            text = """
                {
                    "groups": ["0000-En-To-Tre"]
                }
            """.trimIndent(),
            contentType = ContentType.Application.Json
        )
    }
}