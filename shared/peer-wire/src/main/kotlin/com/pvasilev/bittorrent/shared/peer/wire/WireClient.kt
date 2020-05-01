package com.pvasilev.bittorrent.shared.peer.wire

import com.pvasilev.bittorrent.shared.torrent.MetaInfo
import java.io.File
import java.net.Socket

class WireClient(
    private val peerId: String,
    private val socket: Socket,
    private val outputDir: File,
    private val metaInfo: MetaInfo
) {
    private val outputStream = socket.getOutputStream()
    private val inputStream = socket.getInputStream()

    fun connect() {
        val handshake = Message.Handshake(metaInfo.infoHash, peerId)
        outputStream.write(handshake.toByteArray())
        val buffer = inputStream.readNBytes(359)
        var startIndex = 0
        while (startIndex < buffer.size) {
            val message = Message.from(buffer.sliceArray(startIndex until buffer.size))
            startIndex += when (message) {
                is Message.Handshake -> 68
                is Message.Bitfield -> message.flags.size + 5
                else -> 9
            }
        }
        outputStream.write(Message.Interested.toByteArray())
        inputStream.readNBytes(5)
    }

    fun load(pieceIndex: Int) {
        val parent = File(outputDir, metaInfo.infoHash)
        if (!parent.exists()) {
            parent.mkdir()
        }
        val file = File(parent, "piece_$pieceIndex")
        val length = 1024
        var offset = file.length().toInt()
        while (offset < metaInfo.info.pieceLength) {
            val request = Message.Request(pieceIndex, offset, length)
            outputStream.write(request.toByteArray())
            val bytes = inputStream.readNBytes(length + 13)
            val piece = Message.from(bytes) as Message.Piece
            offset += piece.block.size
            file.appendBytes(piece.block)
        }
    }

    fun disconnect() {
        socket.close()
    }
}