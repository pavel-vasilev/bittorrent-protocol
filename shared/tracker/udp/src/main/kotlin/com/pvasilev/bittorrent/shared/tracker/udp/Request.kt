package com.pvasilev.bittorrent.shared.tracker.udp

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

sealed class Request {
    class Connect(private val transactionId: Int) : Request() {
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
        private val connectionId: Long,
        private val transactionId: Int,
        private val infoHash: String,
        private val peerId: String,
        private val downloaded: Long,
        private val left: Long,
        private val uploaded: Long,
        private val numWant: Int,
        private val port: Int
    ) : Request() {
        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream(16)
            val dos = DataOutputStream(os)
            dos.writeLong(connectionId)
            dos.writeInt(ACTION_ANNOUNCE)
            dos.writeInt(transactionId)
            dos.writeUTF(infoHash)
            dos.writeUTF(peerId)
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
        private val connectionId: Long,
        private val transactionId: Int,
        private val infoHashes: List<String>
    ) : Request() {
        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream(16 + 20 * infoHashes.size)
            val dos = DataOutputStream(os)
            dos.writeLong(connectionId)
            dos.writeInt(ACTION_SCRAPE)
            dos.writeInt(transactionId)
            infoHashes.forEach { dos.writeUTF(it) }
            return os.toByteArray()
        }
    }

    abstract fun toByteArray(): ByteArray
}