package com.vibetodo.db.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.vibetodo.TodoDbQueries
import com.vibetodo.db.TodoDb
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<TodoDb>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = TodoDbImpl.Schema

internal fun KClass<TodoDb>.newInstance(driver: SqlDriver): TodoDb = TodoDbImpl(driver)

private class TodoDbImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), TodoDb {
  override val todoDbQueries: TodoDbQueries = TodoDbQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE TodoEntity (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    description TEXT NOT NULL DEFAULT '',
          |    dueDate INTEGER,
          |    dueTimeMinutes INTEGER,
          |    priority TEXT NOT NULL DEFAULT 'Medium',
          |    categoryId TEXT,
          |    isCompleted INTEGER NOT NULL DEFAULT 0,
          |    isRecurring INTEGER NOT NULL DEFAULT 0,
          |    recurrenceRule TEXT,
          |    createdAt INTEGER NOT NULL,
          |    updatedAt INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
