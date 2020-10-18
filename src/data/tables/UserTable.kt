package com.kismetapps.data.tables

import com.kismetapps.models.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable(name = "users") {
    val email = varchar("email", 128).uniqueIndex()
    val displayName = varchar("display_name", 256)
    val passwordHash = varchar("password_hash", 64)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UserTable)

    var email by UserTable.email
    var displayName by UserTable.email
    var passwordHash by UserTable.passwordHash

    fun toUser(): User {
        return User(
                id = this.readValues[UserTable.id].value,
                email = this.readValues[UserTable.email],
                displayName = this.readValues[UserTable.displayName],
                passwordHash = this.readValues[UserTable.passwordHash]
        )
    }
}