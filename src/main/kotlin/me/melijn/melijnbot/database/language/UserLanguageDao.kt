package me.melijn.melijnbot.database.language

import me.melijn.melijnbot.database.CacheDBDao
import me.melijn.melijnbot.database.DriverManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserLanguageDao(driverManager: DriverManager) : CacheDBDao(driverManager) {

    override val table: String = "userLanguages"
    override val tableStructure: String = "userId bigint, language varchar(32)"
    override val primaryKey: String = "userId"

    override val cacheName: String = "language:user"

    init {
        driverManager.registerTable(table, tableStructure, primaryKey)
    }

    suspend fun get(userId: Long): String = suspendCoroutine {
        driverManager.executeQuery("SELECT * FROM $table WHERE userId = ?", { resultset ->
            if (resultset.next()) {
                it.resume(resultset.getString("language"))
            } else {
                it.resume("")
            }

        }, userId)
    }

    fun set(userId: Long, language: String) {
        driverManager.executeUpdate("INSERT INTO $table (userId, language) VALUES (?, ?) ON CONFLICT ($primaryKey) DO UPDATE SET language = ?",
            userId, language, language)
    }

    fun remove(userId: Long) {
        driverManager.executeUpdate("DELETE FROM $table WHERE userId = ?", userId)
    }
}