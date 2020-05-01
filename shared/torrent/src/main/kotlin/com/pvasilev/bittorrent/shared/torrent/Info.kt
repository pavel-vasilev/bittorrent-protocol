package com.pvasilev.bittorrent.shared.torrent

data class Info(val name: String, val pieceLength: Int) {
    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_PIECE_LENGTH = "piece length"

        fun from(map: Map<String, Any>) = Info(
            name = map[KEY_NAME] as String,
            pieceLength = map[KEY_PIECE_LENGTH] as Int
        )
    }
}