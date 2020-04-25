package com.pvasilev.bittorrent.shared.tracker.udp

import java.io.ByteArrayInputStream
import java.io.DataInputStream

sealed class Response {
    class Connect(val transactionId: Int, val connectionId: Long) : Response() {
        companion object {
            fun from(buffer: ByteArray): Connect {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                val actionId = dis.readInt()
                val transactionId = dis.readInt()
                val connectionId = dis.readLong()
                if (actionId != ACTION_CONNECT) throw IllegalStateException()
                return Connect(transactionId, connectionId)
            }
        }
    }

    class Announce(
        val transactionId: Int,
        val interval: Int,
        val leechers: Int,
        val seeders: Int
    ) : Response() {
        companion object {
            fun from(buffer: ByteArray): Announce {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                val actionId = dis.readInt()
                val transactionId = dis.readInt()
                val interval = dis.readInt()
                val leechers = dis.readInt()
                val seeders = dis.readInt()
                if (actionId != ACTION_ANNOUNCE) throw IllegalStateException()
                return Announce(transactionId, interval, leechers, seeders)
            }
        }
    }

    class Scrape(val transactionId: Int) : Response() {
        companion object {
            fun from(buffer: ByteArray): Scrape {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                val actionId = dis.readInt()
                val transactionId = dis.readInt()
                if (actionId != ACTION_SCRAPE) throw IllegalStateException()
                return Scrape(transactionId)
            }
        }
    }
}