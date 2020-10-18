package com.kismetapps.data.repository

import com.kismetapps.data.DatabaseFactory.dbQuery
import com.kismetapps.data.tables.UserEntity
import com.kismetapps.data.tables.UserTable
import com.kismetapps.models.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.InsertStatement

class ReUpRepository : Repository {

    // DAO insert doesn't work... need to figure out why
    override suspend fun addUser(email: String, displayName: String, passwordHash: String): User? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = UserTable.insert { userTable ->
                userTable[UserTable.email] = email
                userTable[UserTable.displayName] = displayName
                userTable[UserTable.passwordHash] = passwordHash
            }
        }
        return rowToUser(statement?.resultedValues?.get(0))
    }

    private fun rowToUser(row: ResultRow?): User? {
        if (row == null) {
            return null
        }
        return User(
                id = row[UserTable.id].value,
                email = row[UserTable.email],
                displayName = row[UserTable.displayName],
                passwordHash = row[UserTable.passwordHash]
        )
    }

    override suspend fun getUsers() = dbQuery {
        UserEntity.all().map {
            it.toUser()
        }
    }

    override suspend fun findUserById(id: Int) = dbQuery {
        UserEntity.findById(id)?.toUser()
    }

    override suspend fun findUserByEmail(email: String) = dbQuery {
        UserEntity.find {
            UserTable.email eq email
        }.map {
            it.toUser()
        }.singleOrNull()
    }

}