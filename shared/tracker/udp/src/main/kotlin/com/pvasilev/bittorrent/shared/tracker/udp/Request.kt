package com.pvasilev.bittorrent.shared.tracker.udp

import com.pvasilev.bittorrent.shared.torrent.decodeHex
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

sealed class Request {
    class Connect(val transactionId: Int) : Request() {
        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream(16)
            val dos = DataOutputStream(os)
            dos.writeLong(INITIAL_CONNECTION_ID)
            dos.writeInt(ACTION_CONNECT)
            dos.writeInt(transactionId)
            return os.toByteArray()
        }
    }

    class Announce(
        val connectionId: Long,
        val transactionId: Int,
        val infoHash: String,
        val peerId: String,
        val downloaded: Long,
        val left: Long,
        val uploaded: Long,
        val numWant: Int,
        val port: Int
    ) : Request() {
        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream(16)
            val dos = DataOutputStream(os)
            dos.writeLong(connectionId)
            dos.writeInt(ACTION_ANNOUNCE)
            dos.writeInt(transactionId)
            dos.write(decodeHex(infoHash))
            dos.writeBytes(peerId)
            dos.writeLong(downloaded)
            dos.writeLong(left)
            dos.writeLong(uploaded)
            dos.writeInt(0)
            dos.writeInt(0)
            dos.writeInt(0)
            dos.writeInt(numWant)
            dos.writeShort(port)
            return os.toByteArray()
        }
    }

    class Scrape(
        val connectionId: Long,
        val transactionId: Int,
        val infoHashes: List<String>
    ) : Request() {
        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream(16 + 20 * infoHashes.size)
            val dos = DataOutputStream(os)
            dos.writeLong(connectionId)
            dos.writeInt(ACTION_SCRAPE)
            dos.writeInt(transactionId)
            infoHashes.forEach { dos.write(decodeHex(it)) }
            return os.toByteArray()
        }
    }

    abstract fun toByteArray(): ByteArray
}