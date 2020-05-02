package com.pvasilev.bittorrent.shared.torrent

import com.pvasilev.bittorrent.shared.bencoding.decodeMap
import com.pvasilev.bittorrent.shared.bencoding.nextToken
import java.io.File
import java.security.MessageDigest
import java.time.Instant

data class MetaInfo(
    val announce: String,
    val infoHash: String,
    val info: Info,
    val comment: String?,
    val createdBy: String?,
    val createdAt: Instant?
) {
    companion object {
        private const val KEY_ANNOUNCE = "announce"
        private const val KEY_COMMENT = "comment"
        private const val KEY_CREATED_BY = "created by"
        private const val KEY_CREATION_DATE = "creation date"
        private const val KEY_INFO = "info"

        fun from(file: File): MetaInfo {
            val bytes = file.readBytes()

            val map = decodeMap(bytes, Charsets.ISO_8859_1)

            val startIndex = bytes.toList().windowed(4).indexOf(KEY_INFO.map(Char::toByte)) + 4
            val info = bytes.nextToken(startIndex)

            val digester = MessageDigest.getInstance("SHA-1")
            val digest = digester.digest(info)
            val infoHash = digest.joinToString(separator = "") { String.format("%02x", it) }

            return MetaInfo(
                announce = map[KEY_ANNOUNCE] as String,
                infoHash = infoHash,
                info = (map[KEY_INFO] as Map<String, Any>).let(Info.Companion::from),
                comment = map[KEY_COMMENT] as String?,
                createdBy = map[KEY_CREATED_BY] as String?,
                createdAt = (map[KEY_CREATION_DATE] as Int?)?.let(Int::toLong)?.let(Instant::ofEpochSecond)
            )
        }
    }
}