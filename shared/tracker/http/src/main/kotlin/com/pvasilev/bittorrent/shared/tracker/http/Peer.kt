package com.pvasilev.bittorrent.shared.tracker.http

data class Peer(val id: String, val ip: String, val port: Int) {
    companion object {
        private const val KEY_PEER_ID = "peer id"
        private const val KEY_IP = "ip"
        private const val KEY_PORT = "port"

        fun from(map: Map<String, Any>) = Peer(
            id = map[KEY_PEER_ID] as String,
            ip = map[KEY_IP] as String,
            port = map[KEY_PORT] as Int
        )
    }
}