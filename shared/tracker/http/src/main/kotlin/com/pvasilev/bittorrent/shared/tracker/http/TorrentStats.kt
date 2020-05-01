package com.pvasilev.bittorrent.shared.tracker.http

data class TorrentStats(val infoHash: String, val seeders: Int, val leechers: Int)