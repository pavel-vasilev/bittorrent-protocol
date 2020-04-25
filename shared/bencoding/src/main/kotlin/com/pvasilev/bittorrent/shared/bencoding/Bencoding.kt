package com.pvasilev.bittorrent.shared.bencoding

fun encodeString(text: String) = "${text.length}:$text"

fun decodeString(text: String) = text.split(":")[1]

fun encodeInt(number: Int) = "i${number}e"

fun decodeInt(text: String): Int {
    val regex = "i([0-9]+)e".toRegex()
    val matchResult = regex.matchEntire(text) ?: throw IllegalStateException()
    return matchResult.groupValues[1].toInt()
}

fun encodeList(list: List<*>): String = list.joinToString(prefix = "l", separator = "", postfix = "e") { value ->
    when (value) {
        is Map<*, *> -> encodeMap(value)
        is List<*> -> encodeList(value)
        is String -> encodeString(value)
        is Int -> encodeInt(value)
        else -> throw IllegalStateException()
    }
}

fun encodeMap(map: Map<*, *>): String = map.asIterable()
    .joinToString(prefix = "d", separator = "", postfix = "e") { (key, value) ->
        encodeString(key.toString()) + when (value) {
            is Map<*, *> -> encodeMap(value)
            is List<*> -> encodeList(value)
            is String -> encodeString(value)
            is Int -> encodeInt(value)
            else -> throw IllegalStateException()
        }
    }