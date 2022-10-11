package no.nav.omsorgspenger.routes

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.omsorgspenger.OpenAm
import no.nav.omsorgspenger.ldap.LdapService
import org.json.JSONArray
import org.json.JSONObject

/**
 * Bruker samme mønster som om man henter grupper fra GraphQL for
 * Et Azure token.
 * https://docs.microsoft.com/en-us/graph/api/user-list-memberof
 */
internal fun Route.ActiveDirectoryRoute(
    openAm: OpenAm,
    ldapService: LdapService
) {

    get("/active-directory/me/memberOf") {
        val userInfo = openAm.verifisertUserInfo(call)
        val grupper = ldapService.hentGrupper(userInfo.navIdent).map {
            JSONObject(
                mapOf(
                    "displayName" to it,
                    "id" to it
                )
            )
        }.let { JSONArray(it) }

        val response = JSONObject().also {
            it.put("value", grupper)
        }

        call.respondText(
            status = HttpStatusCode.OK,
            text = response.toString(),
            contentType = ContentType.Application.Json
        )
    }
}