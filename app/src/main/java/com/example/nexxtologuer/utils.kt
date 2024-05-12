package com.example.nexxtologuer

class utils {
    companion object {
        fun intToByteArray(buffer: ByteArray, offset: Int, data: Int) {
            buffer[offset + 0] = (data shr 0).toByte()
            buffer[offset + 1] = (data shr 8).toByte()
            buffer[offset + 2] = (data shr 16).toByte()
            buffer[offset + 3] = (data shr 24).toByte()
        }
        fun byteArrayToInt(buffer: ByteArray, offset: Int): Int {
            return (buffer[offset + 3].toInt() shl 24) or
                    (buffer[offset + 2].toInt() and 0xff shl 16) or
                    (buffer[offset + 1].toInt() and 0xff shl 8) or
                    (buffer[offset + 0].toInt() and 0xff)
        }
    }
}