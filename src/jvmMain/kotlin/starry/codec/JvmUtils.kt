package starry.codec

import java.io.InputStream
import java.io.OutputStream

fun InputSource.Companion.wrap(source: InputStream): InputSource {
    return object : InputSource {
        override fun available() = source.available()
        override fun next() = source.read().toByte()
        override fun close() = source.close()
    }
}

fun OutputTarget.Companion.wrap(target: OutputStream): OutputTarget {
    return object : OutputTarget {
        override fun write(byte: Byte) = target.write(byte.toInt())
        override fun close() = target.close()
    }
}
