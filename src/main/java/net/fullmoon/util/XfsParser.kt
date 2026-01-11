package net.fullmoon.util

import java.io.*
import java.util.zip.Inflater

/**
 * Class that parses XFS (Xenesis File System) archives. Only "1.0.0.0" versions of XFS files are supported.
 */
class XfsParser(
    val fileStream: FileInputStream,
    val outputFolder: File
) {

    private val stream = DataInputStream(fileStream)
    private var version: Char = '0'
    private var entries: Long = 0L
    private var xfsOffset: Long = 0L

    private var decompressedInfoSize = 0
    private var compressedInfoSize = 0

    init {
        stream.use { read() }
    }

    fun read() {
        val size = fileStream.channel.size()
        val startingOffset = stream.readUnsignedInt() // 32 bit offset
        if (size >= 0xFFFFFFFFL) throw IOException("Provided XFS file is too large to parse.")
        goto(startingOffset)

        val headerSize = stream.readByte()
        val header = decompressZlib(fileStream.peekBytes(headerSize.toInt()), 0x40)
        readHeader(header)

        // Move to the part after the header
        val newOffset = startingOffset + 1 + headerSize
        goto(newOffset)
        if (version != ' ') throw IOException("Encountered unsupported XFS version '$version'")

        decompressedInfoSize = stream.readThreeByteInt()
        compressedInfoSize = stream.readThreeByteInt()

        val infoData = ByteArray(compressedInfoSize)
        stream.readFully(infoData)
        val infoTable = decompressZlib(infoData, decompressedInfoSize)
        extract("", ByteArrayInputStream(infoTable))
    }

    private fun extract(path: String, reader: ByteArrayInputStream) {

        val entries = reader.readUnsignedInt().toInt()

        val nameLength = reader.read()
        val name = reader.readFixedString(nameLength)

        val offset = reader.readVariableSizeLong(6)
        val size = reader.readUnsignedInt()
        val zsize = reader.readUnsignedInt()
        val isFile = reader.read() == 1

        if (isFile) {
            val outputPath = "$path/$name"
            val data = extractFile(outputPath, size, zsize, offset)

            println("Reading $outputPath...")
            val file = File(outputFolder.absolutePath + File.separator + outputPath.replace("/", File.separator))
            file.parentFile.mkdirs()
            file.createNewFile()
            println(" -> Writing ${((data.size / 1024.0) / 1024.0)} MiB(s)")
            file.writeBytes(data)
        }
        repeat(entries) {
            extract("$path/$name", reader)
        }

    }

    private fun extractFile(file: String, size: Long, zsize: Long, offset: Long): ByteArray {
        val endOffset = offset + zsize
        val output = ByteArrayOutputStream()

        var cursor = offset
        while (cursor != endOffset) {
            goto(cursor)

            var chunkSize = stream.readThreeByteInt()
            cursor += 3

            val flags = chunkSize shr 22
            chunkSize = chunkSize and 0x3FFFFF
            var chunkZSize = 0
            val isCompressed = (flags and 1) == 0

            if ((flags and 2) != 0) {
                if (isCompressed) {
                    chunkZSize = stream.readThreeByteInt()
                    cursor += 3
                }
            }
            if (flags == 0) {
                chunkZSize = chunkSize
                chunkSize = 0x10000
            }
            stream.skip(2L) // Two dummy bytes
            cursor += 2

            if (!isCompressed) {
                val data = stream.readNBytes(chunkSize)
                cursor += chunkSize
                output.write(data)
            } else {
                val data = stream.readNBytes(chunkZSize)
                val decompressed = decompressZlib(data, chunkSize)
                cursor += chunkZSize
                output.write(decompressed)
            }
        }
        return output.toByteArray()
    }

    private fun goto(position: Long) {
        fileStream.channel.position(position)
    }

    private fun readHeader(header: ByteArray) {
        val stream = DataInputStream(ByteArrayInputStream(header))
        val magicNumber = stream.readFixedString(3)
        if (magicNumber != "XFS") throw IOException("Unable to read provided file. No magic number ('XFS') found.")

        version = Char(stream.readByte().toInt())
        stream.skip(4) // Dummy data
        entries = stream.readUnsignedInt()
        stream.skip(4) // More dummy data(?)
        xfsOffset = stream.readUnsignedInt()
    }
}

fun decompressZlib(data: ByteArray, bufferSize: Int): ByteArray {
    val inflater = Inflater()
    inflater.setInput(data)

    val output = ByteArrayOutputStream(bufferSize)

    val readBuffer = ByteArray(1024)
    var outputBufferPos = 0
    while (!inflater.finished()) {
        val size = inflater.inflate(readBuffer, 0, readBuffer.size)
        outputBufferPos += size
        output.write(readBuffer, 0, size)
    }

    return output.toByteArray()
}