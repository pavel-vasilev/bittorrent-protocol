package com.pvasilev.bittorrent.shared.tracker.http

sealed class Response {
    class Announce(
        val seeders: Int,
        val leechers: Int,
        val interval: Int,
        val peers: List<Peer>
    ) : Response() {
        companion object {
            private const val KEY_COMPLETE = "complete"
            private const val KEY_INCOMPLETE = "incomplete"
            private const val KEY_INTERVAL = "interval"
            private const val KEY_PEERS = "peers"

            fun from(map: Map<Any, Any>) = Announce(
                seeders = map[KEY_COMPLETE] as Int,
                leechers = map[KEY_INCOMPLETE] as Int,
                interval = map[KEY_INTERVAL] as Int,
                peers = (map[KEY_PEERS] as List<Map<String, Any>>).map(Peer.Companion::from)
            )
        }
    }

    class Scrape(val files: List<TorrentStats>) : Response() {
        companion object {
            private const val KEY_FILES = "files"
            private const val KEY_COMPLETE = "complete"
            private const val KEY_INCOMPLETE = "incomplete"

            fun from(map: Map<Any, Any>): Scrape {
                val files = map[KEY_FILES] as Map<String, Map<String, Any>>
                val torrentStats = files.map { (key, value) ->
                    TorrentStats(
                        infoHash = key,
                        seeders = value[KEY_COMPLETE] as Int,
                        leechers = value[KEY_INCOMPLETE] as Int
                    )
                }
                return Scrape(torrentStats)
            }
        }
    }
}