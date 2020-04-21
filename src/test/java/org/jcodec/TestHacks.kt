package org.jcodec

import org.jcodec.common.DemuxerTrack
import org.jcodec.common.and
import org.jcodec.common.io.FileChannelWrapper
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Packet
import org.jcodec.common.tools.MD5
import java.io.File
import java.lang.StringBuilder
import java.nio.ByteBuffer

fun File.writableChannel(): FileChannelWrapper = NIOUtils.writableChannel(this)

//copies contents of this collection into a one large ByteBuffer
fun Collection<ByteBuffer>.toByteBuffer(): ByteBuffer {
    val sz = sumBy { it.remaining() }
    return fold(ByteBuffer.allocate(sz)) { big, small -> big.put(small); big }.also { it.flip() }
}

//copies contents of this collection into a one large ByteArray
fun Collection<ByteBuffer>.toByteArray(): ByteArray {
    val sz = sumBy { it.remaining() }

    return fold(0 to ByteArray(sz)) { (off, big), small ->
        val duplicate = small.duplicate()
        val sz = duplicate.remaining()
        duplicate.get(big, off, sz)
        off + sz to big
    }.second
}

fun ByteArray.md5(): String = MD5.md5sumBytes(this)

//toByteBuffer would make a copy of data vs asByteBuffer represents the same data as ByteBuffer
fun ByteArray.asByteBuffer(): ByteBuffer = ByteBuffer.wrap(this)

//copies contents of ByteBuffer to new ByteArray
fun ByteBuffer.toByteArray(): ByteArray = NIOUtils.toArray(this)

fun ByteBuffer.hexDump(): String = HexDump.hexdump(this)
val _hexLUT = (0..256).map { String.format("%02x", it) }.toTypedArray()
fun ByteArray.toHex(): String {
    return fold(StringBuilder()) { sb, b -> sb.append(_hexLUT[(b and 0xff)]) }.toString()
}

fun String.parseHex(): ByteArray {
    val arr = ByteArray(this.length / 2)
    var j = 0
    for (i in this.indices step 2) {
        arr[j++] = substring(i, i + 2).toInt(16).toByte()
    }
    return arr
}

fun DemuxerTrack.asSequence(): Sequence<Packet> = generateSequence { nextFrame() }