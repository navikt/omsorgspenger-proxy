package no.nav.omsorgspenger.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspenger.OpenAm
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("no.nav.ActiveDirectoryRoute")

/**
 * Bruker samme mønster som om man henter grupper fra GraphQL for
 * Et Azure token.
 * https://docs.microsoft.com/en-us/graph/api/user-list-memberof
 */
internal fun Route.ActiveDirectoryRoute(
    openAm: OpenAm) {

    get("/active-directory/me/memberOf") {
        val userInfo = openAm.verifisertUserInfo(call)

        call.respondText(
            status = HttpStatusCode.OK,
            text = """
            {
                "value": [{
                    "displayName": "Contoso-tier Query Notification",
                    "id": "11111111-2222-3333-4444-555555555555"
                }]
            }
            """.trimIndent(),
            contentType = ContentType.Application.Json
        )
    }
}