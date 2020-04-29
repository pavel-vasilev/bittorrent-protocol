package com.pvasilev.bittorrent.shared.bencoding

fun encodeString(text: String) = "${text.length}:$text"

fun decodeString(text: String) = text.substringAfter(":")

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

fun decodeList(text: String): List<Any> {
    val result = mutableListOf<Any>()
    var index = 1

    while (index != text.length - 1) {
        val token = text.substring(index).nextToken()

        val value = when (text[index]) {
            'd' -> decodeMap(token)
            'l' -> decodeList(token)
            'i' -> decodeInt(token)
            in '0'..'9' -> decodeString(token)
            else -> throw IllegalStateException()
        }

        index += token.length
        result += value
    }

    return result
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

fun decodeMap(text: String): Map<Any, Any> {
    val result = mutableMapOf<Any, Any>()
    var index = 1
    var key: Any? = null

    while (index != text.length - 1) {
        val token = text.substring(index).nextToken()

        val value = when (text[index]) {
            'd' -> decodeMap(token)
            'l' -> decodeList(token)
            'i' -> decodeInt(token)
            in '0'..'9' -> decodeString(token)
            else -> throw IllegalStateException()
        }

        index += token.length

        if (key == null) {
            key = value
        } else {
            result[key] = value
            key = null
        }
    }

    return result
}

private fun String.nextToken(startIndex: Int = 0): String =
    when (this[startIndex]) {
        'd', 'l' -> {
            var endIndex = startIndex + 1
            while (this[endIndex] != 'e') {
                endIndex += nextToken(endIndex).length
            }
            substring(startIndex, endIndex + 1)
        }
        'i' -> {
            substring(startIndex).takeWhileInclusive { it != 'e' }
        }
        in '0'..'9' -> {
            val length = substring(startIndex)
                .takeWhile { it in '0'..'9' }
                .toInt()
            val endIndex = startIndex + length.digits() + 1 + length
            substring(startIndex, endIndex)
        }
        else -> throw IllegalStateException()
    }

private fun String.takeWhileInclusive(pred: (Char) -> Boolean): String {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = pred(it)
        result
    }
}

private fun Int.digits(): Int {
    var length = 0
    var n = this
    while (n != 0) {
        n /= 10
        length++
    }
    return length
}