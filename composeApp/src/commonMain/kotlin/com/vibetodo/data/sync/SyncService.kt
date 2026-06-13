package com.vibetodo.data.sync

import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.domain.model.IdGenerator
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SyncTodoItem(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: Long?,
    val dueTimeMinutes: Long?,
    val priority: String,
    val categoryId: String?,
    val isCompleted: Boolean,
    val isRecurring: Boolean,
    val recurrenceRule: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class SyncData(
    val version: Int = 1,
    val todos: List<SyncTodoItem>,
    val exportedAt: Long,
)

class SyncService(private val dbHelper: DatabaseHelper) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    suspend fun exportEncryptedBackup(passphrase: String, filePath: String): Boolean {
        try {
            val entities = dbHelper.getAllTodosSnapshot()
            val items = entities.map { e ->
                SyncTodoItem(
                    id = e.id,
                    title = e.title,
                    description = e.description,
                    dueDate = e.dueDate,
                    dueTimeMinutes = e.dueTimeMinutes,
                    priority = e.priority,
                    categoryId = e.categoryId,
                    isCompleted = e.isCompleted != 0L,
                    isRecurring = e.isRecurring != 0L,
                    recurrenceRule = e.recurrenceRule,
                    createdAt = e.createdAt,
                    updatedAt = e.updatedAt,
                )
            }
            val syncData = SyncData(todos = items, exportedAt = Clock.System.now().toEpochMilliseconds())
            val plaintext = json.encodeToString(syncData).encodeToByteArray()
            val encrypted = aesEncrypt(plaintext, passphrase)
            return writeFileBytes(filePath, encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun importEncryptedBackup(filePath: String, passphrase: String): Boolean {
        try {
            val encrypted = readFileBytes(filePath) ?: return false
            val plaintext = aesDecrypt(encrypted, passphrase)
            val syncData = json.decodeFromString<SyncData>(plaintext.decodeToString())
            val todos = syncData.todos.map { item ->
                com.vibetodo.TodoEntity(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    dueDate = item.dueDate,
                    dueTimeMinutes = item.dueTimeMinutes,
                    priority = item.priority,
                    categoryId = item.categoryId,
                    isCompleted = if (item.isCompleted) 1L else 0L,
                    isRecurring = if (item.isRecurring) 1L else 0L,
                    recurrenceRule = item.recurrenceRule,
                    createdAt = item.createdAt,
                    updatedAt = item.updatedAt,
                )
            }
            dbHelper.deleteAllTodos()
            dbHelper.insertAll(todos)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun syncToDropbox(accessToken: String, passphrase: String): Boolean {
        try {
            val entities = dbHelper.getAllTodosSnapshot()
            val items = entities.map { e ->
                SyncTodoItem(
                    id = e.id,
                    title = e.title,
                    description = e.description,
                    dueDate = e.dueDate,
                    dueTimeMinutes = e.dueTimeMinutes,
                    priority = e.priority,
                    categoryId = e.categoryId,
                    isCompleted = e.isCompleted != 0L,
                    isRecurring = e.isRecurring != 0L,
                    recurrenceRule = e.recurrenceRule,
                    createdAt = e.createdAt,
                    updatedAt = e.updatedAt,
                )
            }
            val syncData = SyncData(todos = items, exportedAt = Clock.System.now().toEpochMilliseconds())
            val plaintext = json.encodeToString(syncData).encodeToByteArray()
            val encrypted = aesEncrypt(plaintext, passphrase)

            val dropboxArg = """{"path": "/qululist_backup.enc", "mode": "overwrite"}"""
            val headers = mapOf(
                "Authorization" to "Bearer $accessToken",
                "Dropbox-API-Arg" to dropboxArg,
                "Content-Type" to "application/octet-stream",
            )
            val (status, _) = httpPost("https://content.dropboxapi.com/2/files/upload", headers, encrypted)
            return status == 200
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun syncFromDropbox(accessToken: String, passphrase: String): Boolean {
        try {
            val dropboxArg = """{"path": "/qululist_backup.enc"}"""
            val headers = mapOf(
                "Authorization" to "Bearer $accessToken",
                "Dropbox-API-Arg" to dropboxArg,
            )
            val (status, encrypted) = httpPost(
                "https://content.dropboxapi.com/2/files/download",
                headers,
                ByteArray(0),
            )
            if (status != 200) return false

            val plaintext = aesDecrypt(encrypted, passphrase)
            val syncData = json.decodeFromString<SyncData>(plaintext.decodeToString())
            val todos = syncData.todos.map { item ->
                com.vibetodo.TodoEntity(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    dueDate = item.dueDate,
                    dueTimeMinutes = item.dueTimeMinutes,
                    priority = item.priority,
                    categoryId = item.categoryId,
                    isCompleted = if (item.isCompleted) 1L else 0L,
                    isRecurring = if (item.isRecurring) 1L else 0L,
                    recurrenceRule = item.recurrenceRule,
                    createdAt = item.createdAt,
                    updatedAt = item.updatedAt,
                )
            }
            dbHelper.deleteAllTodos()
            dbHelper.insertAll(todos)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getDropboxAuthUrl(clientId: String): String {
        return "https://www.dropbox.com/oauth2/authorize?client_id=$clientId&response_type=code&redirect_uri=http://localhost"
    }
}
