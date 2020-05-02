package com.pvasilev.bittorrent.shared.torrent

data class Info(val name: String, val pieceLength: Int, val pieces: List<ByteArray>) {
    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_PIECE_LENGTH = "piece length"
        private const val KEY_PIECES = "pieces"
        private const val SHA1_SIZE = 20

        fun from(map: Map<String, Any>) = Info(
            name = map[KEY_NAME] as String,
            pieceLength = map[KEY_PIECE_LENGTH] as Int,
            pieces = (map[KEY_PIECES] as String).chunked(SHA1_SIZE).map { it.toByteArray(Charsets.ISO_8859_1) }
        )
    }
}