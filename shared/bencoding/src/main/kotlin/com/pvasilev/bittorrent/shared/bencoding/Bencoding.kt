package com.pvasilev.bittorrent.shared.bencoding

private const val DICTIONARY: Byte = 100
private const val LIST: Byte = 108
private const val INTEGER: Byte = 105
private const val END: Byte = 101
private const val ZERO: Byte = 48
private const val NINE: Byte = 57
private const val COLON: Byte = 58

fun encodeString(text: String) = "${text.length}:$text"

fun decodeString(bytes: ByteArray) = bytes.sliceArray(bytes.indexOf(COLON) + 1 until bytes.size)
    .toString(Charsets.UTF_8)

fun decodeString(text: String): String = decodeString(text.toByteArray())

fun encodeInt(number: Int) = "i${number}e"

fun decodeInt(bytes: ByteArray): Int {
    val regex = "i([0-9]+)e".toRegex()
    val matchResult = regex.matchEntire(String(bytes)) ?: throw IllegalStateException()
    return matchResult.groupValues[1].toInt()
}

fun decodeInt(text: String): Int = decodeInt(text.toByteArray())

fun encodeList(list: List<*>): String = list.joinToString(prefix = "l", separator = "", postfix = "e") { value ->
    when (value) {
        is Map<*, *> -> encodeMap(value)
        is List<*> -> encodeList(value)
        is String -> encodeString(value)
        is Int -> encodeInt(value)
        else -> throw IllegalStateException()
    }
}

fun decodeList(bytes: ByteArray): List<Any> {
    val result = mutableListOf<Any>()
    var index = 1

    while (index != bytes.size - 1) {
        val token = bytes.sliceArray(index until bytes.size).nextToken()

        val value = when (bytes[index]) {
            DICTIONARY -> decodeMap(token)
            LIST -> decodeList(token)
            INTEGER -> decodeInt(token)
            in ZERO..NINE -> decodeString(token)
            else -> throw IllegalStateException()
        }

        index += token.size
        result += value
    }

    return result
}

fun decodeList(text: String): List<Any> = decodeList(text.toByteArray())

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

fun decodeMap(bytes: ByteArray): Map<Any, Any> {
    val result = mutableMapOf<Any, Any>()
    var index = 1
    var key: Any? = null

    while (index != bytes.size - 1) {
        val token = bytes.sliceArray(index until bytes.size).nextToken()

        val value = when (bytes[index]) {
            DICTIONARY -> decodeMap(token)
            LIST -> decodeList(token)
            INTEGER -> decodeInt(token)
            in ZERO..NINE -> decodeString(token)
            else -> throw IllegalStateException()
        }

        index += token.size

        if (key == null) {
            key = value
        } else {
            result[key] = value
            key = null
        }
    }

    return result
}

fun decodeMap(text: String): Map<Any, Any> = decodeMap(text.toByteArray())

fun ByteArray.nextToken(startIndex: Int = 0): ByteArray =
    when (this[startIndex]) {
        DICTIONARY, LIST -> {
            var endIndex = startIndex + 1
            while (this[endIndex] != END) {
                endIndex += nextToken(endIndex).size
            }
            sliceArray(startIndex until endIndex + 1)
        }
        INTEGER -> {
            sliceArray(startIndex until size).takeWhileInclusive { it != END }.toByteArray()
        }
        in ZERO..NINE -> {
            val length = slice(startIndex until size)
                .takeWhile { it in ZERO..NINE }
                .map { it.toChar() }
                .joinToString(separator = "")
                .toInt()
            val endIndex = startIndex + length.digits() + 1 + length
            sliceArray(startIndex until endIndex)
        }
        else -> throw IllegalStateException()
    }

private fun ByteArray.takeWhileInclusive(pred: (Byte) -> Boolean): List<Byte> {
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