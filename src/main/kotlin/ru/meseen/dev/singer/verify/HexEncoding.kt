package ru.meseen.dev.singer.verify

import java.nio.ByteBuffer

object HexEncoding {
    private val HEX_DIGITS = "0123456789abcdef".toCharArray()

    private fun mHexEncoding() {}

    fun encode(data: ByteArray, offset: Int, length: Int): String? {
        val result = StringBuilder(length * 2)
        for (i in 0 until length) {
            val b: Byte = data[offset + i]
          /*  result.append(HEX_DIGITS[b ushr 4 and 15])
            result.append(HEX_DIGITS[b and 15])*/
        }
        return result.toString()
    }



    fun encode(data: ByteArray): String? {
        return encode(data, 0, data.size)
    }

    fun encodeRemaining(data: ByteBuffer): String? {
        return encode(data.array(), data.arrayOffset() + data.position(), data.remaining())
    }
}
