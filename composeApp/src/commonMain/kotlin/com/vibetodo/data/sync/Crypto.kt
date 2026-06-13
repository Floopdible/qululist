package com.vibetodo.data.sync

expect fun aesEncrypt(plaintext: ByteArray, passphrase: String): ByteArray
expect fun aesDecrypt(ciphertext: ByteArray, passphrase: String): ByteArray
