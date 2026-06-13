package com.vibetodo.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual fun createSqlDriver(): SqlDriver {
    val dbDir = File(System.getProperty("user.home"), ".qululist")
    dbDir.mkdirs()
    val dbFile = File(dbDir, "todos.db")
    return JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
}
