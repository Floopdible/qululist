package com.vibetodo.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.vibetodo.TodoDbQueries
import com.vibetodo.db.composeApp.newInstance
import com.vibetodo.db.composeApp.schema
import kotlin.Unit

public interface TodoDb : Transacter {
  public val todoDbQueries: TodoDbQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = TodoDb::class.schema

    public operator fun invoke(driver: SqlDriver): TodoDb = TodoDb::class.newInstance(driver)
  }
}
