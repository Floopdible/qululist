package com.vibetodo

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class TodoDbQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (
    id: String,
    title: String,
    description: String,
    dueDate: Long?,
    dueTimeMinutes: Long?,
    priority: String,
    categoryId: String?,
    isCompleted: Long,
    isRecurring: Long,
    recurrenceRule: String?,
    createdAt: Long,
    updatedAt: Long,
  ) -> T): Query<T> = Query(39_487_330, arrayOf("TodoEntity"), driver, "TodoDb.sq", "selectAll",
      "SELECT TodoEntity.id, TodoEntity.title, TodoEntity.description, TodoEntity.dueDate, TodoEntity.dueTimeMinutes, TodoEntity.priority, TodoEntity.categoryId, TodoEntity.isCompleted, TodoEntity.isRecurring, TodoEntity.recurrenceRule, TodoEntity.createdAt, TodoEntity.updatedAt FROM TodoEntity ORDER BY updatedAt DESC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3),
      cursor.getLong(4),
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getString(9),
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectAll(): Query<TodoEntity> = selectAll { id, title, description, dueDate,
      dueTimeMinutes, priority, categoryId, isCompleted, isRecurring, recurrenceRule, createdAt,
      updatedAt ->
    TodoEntity(
      id,
      title,
      description,
      dueDate,
      dueTimeMinutes,
      priority,
      categoryId,
      isCompleted,
      isRecurring,
      recurrenceRule,
      createdAt,
      updatedAt
    )
  }

  public fun <T : Any> selectById(id: String, mapper: (
    id: String,
    title: String,
    description: String,
    dueDate: Long?,
    dueTimeMinutes: Long?,
    priority: String,
    categoryId: String?,
    isCompleted: Long,
    isRecurring: Long,
    recurrenceRule: String?,
    createdAt: Long,
    updatedAt: Long,
  ) -> T): Query<T> = SelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3),
      cursor.getLong(4),
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getString(9),
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectById(id: String): Query<TodoEntity> = selectById(id) { id_, title, description,
      dueDate, dueTimeMinutes, priority, categoryId, isCompleted, isRecurring, recurrenceRule,
      createdAt, updatedAt ->
    TodoEntity(
      id_,
      title,
      description,
      dueDate,
      dueTimeMinutes,
      priority,
      categoryId,
      isCompleted,
      isRecurring,
      recurrenceRule,
      createdAt,
      updatedAt
    )
  }

  public fun <T : Any> selectByCompletion(isCompleted: Long, mapper: (
    id: String,
    title: String,
    description: String,
    dueDate: Long?,
    dueTimeMinutes: Long?,
    priority: String,
    categoryId: String?,
    isCompleted: Long,
    isRecurring: Long,
    recurrenceRule: String?,
    createdAt: Long,
    updatedAt: Long,
  ) -> T): Query<T> = SelectByCompletionQuery(isCompleted) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3),
      cursor.getLong(4),
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getString(9),
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectByCompletion(isCompleted: Long): Query<TodoEntity> =
      selectByCompletion(isCompleted) { id, title, description, dueDate, dueTimeMinutes, priority,
      categoryId, isCompleted_, isRecurring, recurrenceRule, createdAt, updatedAt ->
    TodoEntity(
      id,
      title,
      description,
      dueDate,
      dueTimeMinutes,
      priority,
      categoryId,
      isCompleted_,
      isRecurring,
      recurrenceRule,
      createdAt,
      updatedAt
    )
  }

  public fun <T : Any> selectByPriority(priority: String, mapper: (
    id: String,
    title: String,
    description: String,
    dueDate: Long?,
    dueTimeMinutes: Long?,
    priority: String,
    categoryId: String?,
    isCompleted: Long,
    isRecurring: Long,
    recurrenceRule: String?,
    createdAt: Long,
    updatedAt: Long,
  ) -> T): Query<T> = SelectByPriorityQuery(priority) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3),
      cursor.getLong(4),
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getString(9),
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectByPriority(priority: String): Query<TodoEntity> = selectByPriority(priority) {
      id, title, description, dueDate, dueTimeMinutes, priority_, categoryId, isCompleted,
      isRecurring, recurrenceRule, createdAt, updatedAt ->
    TodoEntity(
      id,
      title,
      description,
      dueDate,
      dueTimeMinutes,
      priority_,
      categoryId,
      isCompleted,
      isRecurring,
      recurrenceRule,
      createdAt,
      updatedAt
    )
  }

  public fun <T : Any> searchByTitle(`value`: String, mapper: (
    id: String,
    title: String,
    description: String,
    dueDate: Long?,
    dueTimeMinutes: Long?,
    priority: String,
    categoryId: String?,
    isCompleted: Long,
    isRecurring: Long,
    recurrenceRule: String?,
    createdAt: Long,
    updatedAt: Long,
  ) -> T): Query<T> = SearchByTitleQuery(value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3),
      cursor.getLong(4),
      cursor.getString(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getString(9),
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun searchByTitle(value_: String): Query<TodoEntity> = searchByTitle(value_) { id, title,
      description, dueDate, dueTimeMinutes, priority, categoryId, isCompleted, isRecurring,
      recurrenceRule, createdAt, updatedAt ->
    TodoEntity(
      id,
      title,
      description,
      dueDate,
      dueTimeMinutes,
      priority,
      categoryId,
      isCompleted,
      isRecurring,
      recurrenceRule,
      createdAt,
      updatedAt
    )
  }

  public fun insert(
    id: String,
    title: String,
    description: String,
    dueDate: Long?,
    dueTimeMinutes: Long?,
    priority: String,
    categoryId: String?,
    isCompleted: Long,
    isRecurring: Long,
    recurrenceRule: String?,
    createdAt: Long,
    updatedAt: Long,
  ) {
    driver.execute(-1_666_991_236, """
        |INSERT INTO TodoEntity(id, title, description, dueDate, dueTimeMinutes, priority, categoryId, isCompleted, isRecurring, recurrenceRule, createdAt, updatedAt)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 12) {
          bindString(0, id)
          bindString(1, title)
          bindString(2, description)
          bindLong(3, dueDate)
          bindLong(4, dueTimeMinutes)
          bindString(5, priority)
          bindString(6, categoryId)
          bindLong(7, isCompleted)
          bindLong(8, isRecurring)
          bindString(9, recurrenceRule)
          bindLong(10, createdAt)
          bindLong(11, updatedAt)
        }
    notifyQueries(-1_666_991_236) { emit ->
      emit("TodoEntity")
    }
  }

  public fun update(
    title: String,
    description: String,
    dueDate: Long?,
    dueTimeMinutes: Long?,
    priority: String,
    categoryId: String?,
    isCompleted: Long,
    isRecurring: Long,
    recurrenceRule: String?,
    updatedAt: Long,
    id: String,
  ) {
    driver.execute(-1_322_045_044, """
        |UPDATE TodoEntity
        |SET title = ?, description = ?, dueDate = ?, dueTimeMinutes = ?, priority = ?, categoryId = ?, isCompleted = ?, isRecurring = ?, recurrenceRule = ?, updatedAt = ?
        |WHERE id = ?
        """.trimMargin(), 11) {
          bindString(0, title)
          bindString(1, description)
          bindLong(2, dueDate)
          bindLong(3, dueTimeMinutes)
          bindString(4, priority)
          bindString(5, categoryId)
          bindLong(6, isCompleted)
          bindLong(7, isRecurring)
          bindString(8, recurrenceRule)
          bindLong(9, updatedAt)
          bindString(10, id)
        }
    notifyQueries(-1_322_045_044) { emit ->
      emit("TodoEntity")
    }
  }

  public fun deleteById(id: String) {
    driver.execute(349_726_560, """DELETE FROM TodoEntity WHERE id = ?""", 1) {
          bindString(0, id)
        }
    notifyQueries(349_726_560) { emit ->
      emit("TodoEntity")
    }
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("TodoEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("TodoEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_224_148_529,
        """SELECT TodoEntity.id, TodoEntity.title, TodoEntity.description, TodoEntity.dueDate, TodoEntity.dueTimeMinutes, TodoEntity.priority, TodoEntity.categoryId, TodoEntity.isCompleted, TodoEntity.isRecurring, TodoEntity.recurrenceRule, TodoEntity.createdAt, TodoEntity.updatedAt FROM TodoEntity WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "TodoDb.sq:selectById"
  }

  private inner class SelectByCompletionQuery<out T : Any>(
    public val isCompleted: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("TodoEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("TodoEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(546_137_842,
        """SELECT TodoEntity.id, TodoEntity.title, TodoEntity.description, TodoEntity.dueDate, TodoEntity.dueTimeMinutes, TodoEntity.priority, TodoEntity.categoryId, TodoEntity.isCompleted, TodoEntity.isRecurring, TodoEntity.recurrenceRule, TodoEntity.createdAt, TodoEntity.updatedAt FROM TodoEntity WHERE isCompleted = ? ORDER BY updatedAt DESC""",
        mapper, 1) {
      bindLong(0, isCompleted)
    }

    override fun toString(): String = "TodoDb.sq:selectByCompletion"
  }

  private inner class SelectByPriorityQuery<out T : Any>(
    public val priority: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("TodoEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("TodoEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_852_426_810,
        """SELECT TodoEntity.id, TodoEntity.title, TodoEntity.description, TodoEntity.dueDate, TodoEntity.dueTimeMinutes, TodoEntity.priority, TodoEntity.categoryId, TodoEntity.isCompleted, TodoEntity.isRecurring, TodoEntity.recurrenceRule, TodoEntity.createdAt, TodoEntity.updatedAt FROM TodoEntity WHERE priority = ? ORDER BY updatedAt DESC""",
        mapper, 1) {
      bindString(0, priority)
    }

    override fun toString(): String = "TodoDb.sq:selectByPriority"
  }

  private inner class SearchByTitleQuery<out T : Any>(
    public val `value`: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("TodoEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("TodoEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(644_972_374,
        """SELECT TodoEntity.id, TodoEntity.title, TodoEntity.description, TodoEntity.dueDate, TodoEntity.dueTimeMinutes, TodoEntity.priority, TodoEntity.categoryId, TodoEntity.isCompleted, TodoEntity.isRecurring, TodoEntity.recurrenceRule, TodoEntity.createdAt, TodoEntity.updatedAt FROM TodoEntity WHERE title LIKE '%' || ? || '%' ORDER BY updatedAt DESC""",
        mapper, 1) {
      bindString(0, value)
    }

    override fun toString(): String = "TodoDb.sq:searchByTitle"
  }
}
