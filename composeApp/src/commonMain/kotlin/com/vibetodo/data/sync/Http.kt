package com.vibetodo.data.sync

expect fun httpPost(
    url: String,
    headers: Map<String, String>,
    body: ByteArray,
): Pair<Int, ByteArray>

expect fun httpGet(
    url: String,
    headers: Map<String, String>,
): Pair<Int, ByteArray>
