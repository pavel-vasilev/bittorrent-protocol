package com.pvasilev.bittorrent.shared.peer.wire

import com.pvasilev.bittorrent.shared.torrent.decodeHex
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

val MESSAGE_HANDSHAKE = "\u0013BitTorrent protocol".toByteArray()
val MESSAGE_KEEP_ALIVE = byteArrayOf(0x0, 0x0, 0x0, 0x0)
val MESSAGE_CHOKE = byteArrayOf(0x0, 0x0, 0x0, 0x1, 0x0)
val MESSAGE_UNCHOKE = byteArrayOf(0x0, 0x0, 0x0, 0x1, 0x1)
val MESSAGE_INTERESTED = byteArrayOf(0x0, 0x0, 0x0, 0x1, 0x2)
val MESSAGE_NOT_INTERESTED = byteArrayOf(0x0, 0x0, 0x0, 0x1, 0x3)
val MESSAGE_HAVE = byteArrayOf(0x0, 0x0, 0x0, 0x5, 0x4)
val MESSAGE_REQUEST = byteArrayOf(0x0, 0x0, 0x0, 0xD, 0x6)
val MESSAGE_CANCEL = byteArrayOf(0x0, 0x0, 0x0, 0xD, 0x8)
val MESSAGE_PORT = byteArrayOf(0x0, 0x0, 0x0, 0x3, 0x9)

fun ByteArray.startsWith(other: ByteArray): Boolean {
    if (other.size > this.size) {
        return false
    }
    return other.withIndex().all { (index, byte) -> byte == this[index] }
}

sealed class Message {
    companion object {
        fun from(buffer: ByteArray): Message {
            return when {
                buffer.startsWith(MESSAGE_HANDSHAKE) -> Handshake.from(buffer)
                buffer.startsWith(MESSAGE_KEEP_ALIVE) -> KeepAlive
                buffer.startsWith(MESSAGE_CHOKE) -> Choke
                buffer.startsWith(MESSAGE_UNCHOKE) -> UnChoke
                buffer.startsWith(MESSAGE_INTERESTED) -> Interested
                buffer.startsWith(MESSAGE_NOT_INTERESTED) -> NotInterested
                buffer.startsWith(MESSAGE_HAVE) -> Have.from(buffer)
                buffer.startsWith(MESSAGE_REQUEST) -> Request.from(buffer)
                buffer.startsWith(MESSAGE_CANCEL) -> Cancel.from(buffer)
                buffer.startsWith(MESSAGE_PORT) -> Port.from(buffer)
                buffer[4] == 0x5.toByte() -> Bitfield.from(buffer)
                buffer[4] == 0x7.toByte() -> Piece.from(buffer)
                else -> throw IllegalStateException()
            }
        }
    }

    abstract fun toByteArray(): ByteArray

    object KeepAlive : Message() {
        override fun toByteArray(): ByteArray = MESSAGE_KEEP_ALIVE
    }

    object Choke : Message() {
        override fun toByteArray(): ByteArray = MESSAGE_CHOKE
    }

    object UnChoke : Message() {
        override fun toByteArray(): ByteArray = MESSAGE_UNCHOKE
    }

    object Interested : Message() {
        override fun toByteArray(): ByteArray = MESSAGE_INTERESTED
    }

    object NotInterested : Message() {
        override fun toByteArray(): ByteArray = MESSAGE_NOT_INTERESTED
    }

    data class Handshake(val infoHash: String, val peerId: String) : Message() {
        companion object {
            fun from(buffer: ByteArray): Handshake {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                dis.skipBytes(MESSAGE_HANDSHAKE.size)
                dis.skip(8)
                val infoHash = dis.readNBytes(20).toString(Charsets.UTF_8)
                val peerId = dis.readNBytes(20).toString(Charsets.UTF_8)
                return Handshake(infoHash, peerId)
            }
        }

        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)
            dos.write(MESSAGE_HANDSHAKE)
            dos.writeLong(0)
            dos.write(decodeHex(infoHash))
            dos.writeBytes(peerId)
            return os.toByteArray()
        }
    }

    data class Request(val pieceIndex: Int, val offset: Int, val length: Int) : Message() {
        companion object {
            fun from(buffer: ByteArray): Request {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                dis.skipBytes(MESSAGE_REQUEST.size)
                val pieceIndex = dis.readInt()
                val offset = dis.readInt()
                val length = dis.readInt()
                return Request(pieceIndex, offset, length)
            }
        }

        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)
            dos.write(MESSAGE_REQUEST)
            dos.writeInt(pieceIndex)
            dos.writeInt(offset)
            dos.writeInt(length)
            return os.toByteArray()
        }
    }

    data class Piece(val pieceIndex: Int, val offset: Int, val block: ByteArray) : Message() {
        companion object {
            fun from(buffer: ByteArray): Piece {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                val length = dis.readInt() - 9
                dis.skip(1)
                val pieceIndex = dis.readInt()
                val offset = dis.readInt()
                val block = dis.readNBytes(length)
                return Piece(pieceIndex, offset, block)
            }
        }

        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)
            dos.writeInt(9 + block.size)
            dos.writeByte(7)
            dos.writeInt(pieceIndex)
            dos.writeInt(offset)
            dos.write(block)
            return os.toByteArray()
        }
    }

    data class Bitfield(val flags: ByteArray) : Message() {
        companion object {
            fun from(buffer: ByteArray): Bitfield {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                val length = dis.readInt() - 1
                dis.skip(1)
                val flags = dis.readNBytes(length)
                return Bitfield(flags)
            }
        }

        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)
            dos.writeInt(1 + flags.size)
            dos.writeByte(5)
            dos.write(flags)
            return os.toByteArray()
        }
    }

    data class Cancel(val pieceIndex: Int, val offset: Int, val length: Int) : Message() {
        companion object {
            fun from(buffer: ByteArray): Cancel {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                dis.skipBytes(MESSAGE_CANCEL.size)
                val pieceIndex = dis.readInt()
                val offset = dis.readInt()
                val length = dis.readInt()
                return Cancel(pieceIndex, offset, length)
            }
        }

        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)
            dos.write(MESSAGE_CANCEL)
            dos.writeInt(pieceIndex)
            dos.writeInt(offset)
            dos.writeInt(length)
            return os.toByteArray()
        }
    }

    data class Have(val pieceIndex: Int) : Message() {
        companion object {
            fun from(buffer: ByteArray): Have {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                dis.skipBytes(MESSAGE_HAVE.size)
                val pieceIndex = dis.readInt()
                return Have(pieceIndex)
            }
        }

        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)
            dos.write(MESSAGE_HAVE)
            dos.writeInt(pieceIndex)
            return os.toByteArray()
        }
    }

    data class Port(val number: Short) : Message() {
        companion object {
            fun from(buffer: ByteArray): Port {
                val dis = DataInputStream(ByteArrayInputStream(buffer))
                dis.skipBytes(MESSAGE_PORT.size)
                val number = dis.readShort()
                return Port(number)
            }
        }

        override fun toByteArray(): ByteArray {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)
            dos.write(MESSAGE_PORT)
            dos.writeShort(number.toInt())
            return os.toByteArray()
        }
    }
}