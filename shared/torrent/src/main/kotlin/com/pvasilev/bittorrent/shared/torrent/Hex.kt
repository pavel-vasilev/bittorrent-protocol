package com.pvasilev.bittorrent.shared.torrent

fun decodeHex(hex: String) = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun encodeHex(bytes: ByteArray) = bytes.joinToString(separator = "") { String.format("%02x", it) }