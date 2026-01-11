package net.fullmoon.util

import java.io.DataInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

fun InputStream.readFixedString(len: Int): String {
    val bytes = readNBytes(len)
    return String(bytes, StandardCharsets.US_ASCII)
}

fun DataInputStream.readThreeByteInt(): Int {
    val bytes = readNBytes(3).toMutableList()
    bytes.addLast(0)
    val buffer = ByteBuffer.wrap(bytes.toByteArray())
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    return buffer.getInt()
}

fun InputStream.readVariableSizeLong(size: Int): Long {
    val bytes = readNBytes(size).toMutableList()
    repeat(8 - size) { bytes.addLast(0) }
    val buffer = ByteBuffer.wrap(bytes.toByteArray())
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    return buffer.getLong()
}

fun InputStream.readUnsignedInt(): Long {
    val bytes = readNBytes(4).toMutableList()
    repeat(4) { bytes.addLast(0) }
    val buffer = ByteBuffer.wrap(bytes.toByteArray())
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    return buffer.getLong()
}

fun FileInputStream.peekBytes(amount: Int): ByteArray {
    val pos = channel.position()
    val data = readNBytes(amount)
    channel.position(pos)
    return data
}
