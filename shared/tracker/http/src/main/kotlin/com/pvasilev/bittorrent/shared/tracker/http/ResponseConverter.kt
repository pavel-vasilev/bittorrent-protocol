package com.pvasilev.bittorrent.shared.tracker.http

import com.pvasilev.bittorrent.shared.bencoding.decodeMap
import okhttp3.ResponseBody
import retrofit2.Converter

sealed class ResponseConverter : Converter<ResponseBody, Response> {
    object Announce : ResponseConverter() {
        override fun convert(body: ResponseBody): Response = convertWithError(body, Response.Announce.Companion::from)
    }

    object Scrape : ResponseConverter() {
        override fun convert(body: ResponseBody): Response = convertWithError(body, Response.Scrape.Companion::from)
    }
}

private fun convertWithError(body: ResponseBody, mapper: (Map<Any, Any>) -> Response): Response {
    val map = decodeMap(body.bytes(), Charsets.ISO_8859_1)
    val failure = map["failure reason"] as String?

    return if (failure != null) {
        throw Exception(failure)
    } else {
        mapper(map)
    }
}