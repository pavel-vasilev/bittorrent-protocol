package com.pvasilev.bittorrent.shared.tracker.udp

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketAddress

const val ACTION_CONNECT = 0
const val ACTION_ANNOUNCE = 1
const val ACTION_SCRAPE = 2
const val ACTION_ERROR = 3
const val INITIAL_CONNECTION_ID = 0x41727101980

class UdpTrackerClient(
    val port: Int,
    val peerId: String,
    val infoHash: String,
    val socket: DatagramSocket,
    val address: SocketAddress
) {
    fun send(request: Request): Response {
        val outputBuffer = request.toByteArray()
        val outputPacket = DatagramPacket(outputBuffer, outputBuffer.size, address)
        socket.send(outputPacket)

        val length = when (request) {
            is Request.Connect -> 16
            is Request.Announce -> 20 + 6 * request.numWant
            is Request.Scrape -> 8 + 12 * request.infoHashes.size
        }

        val inputBuffer = ByteArray(length)
        val inputPacket = DatagramPacket(inputBuffer, inputBuffer.size, address)
        socket.receive(inputPacket)

        return when (request) {
            is Request.Connect -> Response.Connect.from(inputBuffer)
            is Request.Announce -> Response.Announce.from(inputBuffer)
            is Request.Scrape -> Response.Scrape.from(inputBuffer)
        }
    }
}