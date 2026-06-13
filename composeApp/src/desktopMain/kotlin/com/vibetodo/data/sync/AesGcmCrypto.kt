package com.vibetodo.data.sync

import java.io.File
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

actual fun aesEncrypt(plaintext: ByteArray, passphrase: String): ByteArray {
    val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
    val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
    val key = deriveKey(passphrase, salt)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
    val ciphertext = cipher.doFinal(plaintext)
    return salt + iv + ciphertext
}

actual fun aesDecrypt(ciphertext: ByteArray, passphrase: String): ByteArray {
    val salt = ciphertext.copyOfRange(0, 16)
    val iv = ciphertext.copyOfRange(16, 28)
    val encrypted = ciphertext.copyOfRange(28, ciphertext.size)
    val key = deriveKey(passphrase, salt)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
    return cipher.doFinal(encrypted)
}

private fun deriveKey(passphrase: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(passphrase.toCharArray(), salt, 100000, 256)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    return factory.generateSecret(spec).encoded
}

actual fun httpPost(
    url: String,
    headers: Map<String, String>,
    body: ByteArray,
): Pair<Int, ByteArray> {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.connectTimeout = 15000
    connection.readTimeout = 15000
    headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
    if (body.isNotEmpty()) {
        connection.outputStream.use { it.write(body) }
    }
    val status = connection.responseCode
    val response = try {
        connection.inputStream?.readBytes() ?: ByteArray(0)
    } catch (_: Exception) {
        connection.errorStream?.readBytes() ?: ByteArray(0)
    }
    return Pair(status, response)
}

actual fun httpGet(
    url: String,
    headers: Map<String, String>,
): Pair<Int, ByteArray> {
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 15000
    connection.readTimeout = 15000
    headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
    val status = connection.responseCode
    val response = try {
        connection.inputStream?.readBytes() ?: ByteArray(0)
    } catch (_: Exception) {
        connection.errorStream?.readBytes() ?: ByteArray(0)
    }
    return Pair(status, response)
}

actual fun readFileBytes(path: String): ByteArray? {
    val file = File(path)
    return if (file.exists()) file.readBytes() else null
}

actual fun writeFileBytes(path: String, data: ByteArray): Boolean {
    return try {
        File(path).parentFile?.mkdirs()
        File(path).writeBytes(data)
        true
    } catch (_: Exception) {
        false
    }
}
