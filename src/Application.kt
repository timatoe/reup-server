package com.kismetapps

import com.kismetapps.data.DatabaseFactory
import com.kismetapps.data.auth.JwtService
import com.kismetapps.data.auth.ReUpSession
import com.kismetapps.data.auth.hash
import com.kismetapps.data.repository.ReUpRepository
import com.kismetapps.routes.auth
import com.kismetapps.routes.users
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import java.time.Duration
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)

    install(CORS) {
        method(HttpMethod.Options)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.AccessControlAllowOrigin)
        header("Authorization")
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        maxAge = Duration.ofDays(1)
    }

    install(CallLogging)

    install(Locations)

    install(Sessions) {
        cookie<ReUpSession>("REUP_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    DatabaseFactory.init()
    val db = ReUpRepository()
    val jwtService = JwtService()
    val hashFunction = { s: String -> hash(s) }

    install(Authentication) {
        jwt("jwt") {
            verifier(jwtService.verifier)
            realm = "ReUp Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("id")
                val claimString = claim.asInt()
                val user = db.findUserById(claimString)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        auth(db, jwtService, hashFunction)
        users(db)
    }
}

const val API_VERSION = "/v1"
