package com.pvasilev.bittorrent.shared.tracker.http

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackerService {
    @GET("announce")
    fun announce(
        @Query("info_hash") infoHash: String,
        @Query("peer_id") peerId: String,
        @Query("port") port: Int
    ): Call<Response.Announce>

    @GET("scrape")
    fun scrape(
        @Query("info_hash") infoHashes: List<String>,
        @Query("peer_id") peerId: String
    ): Call<Response.Scrape>
}