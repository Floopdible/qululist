package com.vibetodo.data.sync

expect fun readFileBytes(path: String): ByteArray?
expect fun writeFileBytes(path: String, data: ByteArray): Boolean
