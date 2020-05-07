package com.pvasilev.bittorrent.shared.tracker.http

import java.net.Inet4Address
import java.net.InetSocketAddress
import java.nio.ByteBuffer

sealed class Response {
    class Announce(
        val interval: Int,
        val peers: List<InetSocketAddress>
    ) : Response() {
        companion object {
            private const val KEY_INTERVAL = "interval"
            private const val KEY_PEERS = "peers"
            private const val BYTES_PER_PEER = 6

            fun from(map: Map<Any, Any>): Announce {
                val peers = (map[KEY_PEERS] as String)
                    .chunked(BYTES_PER_PEER)
                    .map { it.toByteArray(Charsets.ISO_8859_1) }
                    .map { bytes ->
                        InetSocketAddress(
                            Inet4Address.getByAddress(bytes.sliceArray(0..3)),
                            ByteBuffer.wrap(bytes.sliceArray(4..5)).short.toInt()
                        )
                    }
                val interval = map[KEY_INTERVAL] as Int
                return Announce(interval, peers)
            }
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