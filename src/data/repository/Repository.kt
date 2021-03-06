package com.kismetapps.data.repository

import com.kismetapps.models.User

interface Repository {
    suspend fun addUser(
        email: String,
        displayName: String,
        passwordHash: String
    ): User?

    suspend fun getUsers(): List<User>

    suspend fun findUserById(id: Int): User?

    suspend fun findUserByEmail(email: String): User?
}