package com.pvasilev.bittorrent.shared.tracker.http

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object HttpTrackerConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return when (type) {
            Response.Announce::class.java -> ResponseConverter.Announce
            Response.Scrape::class.java -> ResponseConverter.Scrape
            else -> super.responseBodyConverter(type, annotations, retrofit)
        }
    }
}