package com.kismetapps.routes

import com.kismetapps.API_VERSION
import com.kismetapps.data.auth.JwtService
import com.kismetapps.data.auth.ReUpSession
import com.kismetapps.data.repository.Repository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

const val USERS = "$API_VERSION/users"
const val USER_LOGIN = "$USERS/login"
const val USER_CREATE = "$USERS/create"

@KtorExperimentalLocationsAPI
@Location(USER_LOGIN)
class UserLoginRoute

@KtorExperimentalLocationsAPI
@Location(USER_CREATE)
class UserCreateRoute

@KtorExperimentalLocationsAPI
fun Route.users(
    db: Repository,
    jwtService: JwtService,
    hashFunction: (String) -> String
) {
    post<UserCreateRoute> {
        val signupParameters = call.receive<Parameters>()
        val password = signupParameters["password"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val displayName = signupParameters["displayName"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val email = signupParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val hash = hashFunction(password)
        try {
            val newUser = db.addUser(email, displayName, hash)
            newUser?.userId?.let {
                call.sessions.set(ReUpSession(it))
                call.respondText(
                    jwtService.generateToken(newUser),
                    status = HttpStatusCode.Created
                )
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems creating User")
        }
    }

    post<UserLoginRoute> {
        val signinParameters = call.receive<Parameters>()
        val password = signinParameters["password"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val email = signinParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
        val hash = hashFunction(password)
        try {
            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                if (currentUser.passwordHash == hash) {
                    call.sessions.set(ReUpSession(it))
                    call.respondText(jwtService.generateToken(currentUser))
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest, "Problems retrieving User"
                    )
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
        }
    }

}
