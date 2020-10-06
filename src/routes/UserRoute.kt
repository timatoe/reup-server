package com.kismetapps.routes

import com.kismetapps.API_VERSION
import com.kismetapps.data.auth.JwtService
import com.kismetapps.data.auth.ReUpSession
import com.kismetapps.data.repository.Repository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

const val USER = "$API_VERSION/user"
const val USER_USER = "$USER/user"
const val USER_USERS = "$USER/users"

@KtorExperimentalLocationsAPI
@Location(USER_USER)
class UserRoute

@KtorExperimentalLocationsAPI
@Location(USER_USERS)
class UsersRoute

@KtorExperimentalLocationsAPI
fun Route.users(
    db: Repository
) {
    authenticate("jwt") {
        get<UserRoute> {
            val user = call.sessions.get<ReUpSession>()?.let {
                db.findUserById(it.userId)
            }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Problem retrieving User")
                return@get
            }
            call.respond(user)
        }

        get<UsersRoute> {
            try {
                call.respond(db.getUsers())
            } catch (e: Throwable) {
                application.log.error("Failed to get Users", e)
                call.respond(HttpStatusCode.BadRequest, "Problem getting Users")
            }
        }
    }
}
