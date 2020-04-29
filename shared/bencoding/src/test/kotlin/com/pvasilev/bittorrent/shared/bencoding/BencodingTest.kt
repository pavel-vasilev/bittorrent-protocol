package com.pvasilev.bittorrent.shared.bencoding

import org.junit.Assert.assertEquals
import org.junit.Test

class BencodingTest {
    @Test
    fun `WHEN decode encoded string EXPECT original string`() {
        val text = "hello"

        val encoded = encodeString(text)
        val decoded = decodeString(encoded)

        assertEquals(text, decoded)
    }

    @Test
    fun `WHEN decode encoded integer EXPECT original integer`() {
        val number = 13

        val encoded = encodeInt(number)
        val decoded = decodeInt(encoded)

        assertEquals(number, decoded)
    }

    @Test
    fun `WHEN decode encoded list EXPECT original list`() {
        val list = listOf(1, 2, 3)

        val encoded = encodeList(list)
        val decoded = decodeList(encoded)

        assertEquals(list, decoded)
    }

    @Test
    fun `WHEN decode encoded map EXPECT original map`() {
        val map = mapOf("hello" to "world")

        val encoded = encodeMap(map)
        val decoded = decodeMap(encoded)

        assertEquals(map, decoded)
    }
}