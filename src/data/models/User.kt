package com.kismetapps.models

import io.ktor.auth.*
import java.io.Serializable

data class User(
        val id: Int,
        val email: String,
        val displayName: String,
        val passwordHash: String
) : Serializable, Principal