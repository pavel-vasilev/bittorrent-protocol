package com.pvasilev.bittorrent.shared.tracker.udp

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.net.Inet4Address
import java.net.InetSocketAddress

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
        val seeders: Int,
        val addresses: List<InetSocketAddress>
    ) : Response() {
        companion object {
            fun from(buffer: ByteArray): Announce {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                val actionId = dis.readInt()
                val transactionId = dis.readInt()
                val interval = dis.readInt()
                val leechers = dis.readInt()
                val seeders = dis.readInt()
                val addresses = (0 until dis.available() / 6).map {
                    val addr = dis.readNBytes(4)
                    val port = dis.readUnsignedShort()
                    InetSocketAddress(Inet4Address.getByAddress(addr), port)
                }
                if (actionId != ACTION_ANNOUNCE) throw IllegalStateException()
                return Announce(transactionId, interval, leechers, seeders, addresses)
            }
        }
    }

    class Scrape(val transactionId: Int, val stats: List<Info>) : Response() {

        class Info(val seeders: Int, val completed: Int, val leechers: Int)

        companion object {
            fun from(buffer: ByteArray): Scrape {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                val actionId = dis.readInt()
                val transactionId = dis.readInt()
                val stats = (0 until dis.available() / 12).map {
                    val seeders = dis.readInt()
                    val completed = dis.readInt()
                    val leechers = dis.readInt()
                    Info(seeders, completed, leechers)
                }
                if (actionId != ACTION_SCRAPE) throw IllegalStateException()
                return Scrape(transactionId, stats)
            }
        }
    }
}