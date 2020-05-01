package com.pvasilev.bittorrent.shared.torrent

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class TorrentParsingTest(private val file: String, private val announce: String, private val infoHash: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(
                "big-buck-bunny.torrent",
                "udp://tracker.leechers-paradise.org:6969",
                "dd8255ecdc7ca55fb0bbf81323d87062db1f6d1c"
            ),
            arrayOf(
                "sintel.torrent",
                "udp://tracker.leechers-paradise.org:6969",
                "08ada5a7a6183aae1e09d831df6748d566095a10"
            ),
            arrayOf(
                "tears-of-steel.torrent",
                "udp://tracker.leechers-paradise.org:6969",
                "209c8226b299b308beaf2b9cd3fb49212dbd13ec"
            )
        )
    }

    @Test
    fun `WHEN read torrent file EXPECT get decoded information`() {
        val resource = TorrentParsingTest::class.java.classLoader.getResource(file)
        val file = File(resource.file)

        val metaInfo = MetaInfo.from(file)

        assertEquals(announce, metaInfo.announce)
        assertEquals(infoHash, metaInfo.infoHash)
    }
}