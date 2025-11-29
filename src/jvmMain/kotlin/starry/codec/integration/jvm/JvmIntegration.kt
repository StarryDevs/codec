package starry.codec.integration.jvm

import starry.codec.InputSource
import starry.codec.OutputTarget
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

fun InputSource.Companion.wrap(source: InputStream): InputSource {
    return object : InputSource {
        override fun available() = source.available()
        override fun next() = source.read().toByte()
        override fun close() = source.close()
        override fun skip(n: Long): Long = source.skip(n)
    }
}

fun OutputTarget.Companion.wrap(target: OutputStream): OutputTarget {
    return object : OutputTarget {
        override fun write(byte: Byte) = target.write(byte.toInt())
        override fun close() = target.close()
    }
}

fun InputSource.Companion.wrap(source: ByteBuffer): InputSource {
    return object : InputSource {
        override fun available() = source.remaining()
        override fun next() = source.get()
        override fun close() {}
        override fun skip(n: Long): Long {
            val toSkip = n.coerceAtMost(available().toLong()).toInt()
            source.position(source.position() + toSkip)
            return toSkip.toLong()
        }
    }
}

fun OutputTarget.Companion.wrap(target: ByteBuffer): OutputTarget {
    return object : OutputTarget {
        override fun write(byte: Byte) { target.put(byte) }
        override fun close() {}
    }
}

private class SourceInputStream(private val source: InputSource) : InputStream() {
    override fun read(): Int {
        return if (source.available() > 0) {
            source.next().toInt() and 0xFF
        } else {
            -1
        }
    }

    override fun available(): Int {
        return source.available()
    }

    override fun close() {
        source.close()
    }

    override fun skip(n: Long): Long {
        return source.skip(n)
    }
}

fun InputSource.asInputStream(): InputStream {
    return SourceInputStream(this)
}

private class TargetOutputStream(private val target: OutputTarget) : OutputStream() {
    override fun write(b: Int) {
        target.write(b.toByte())
    }

    override fun close() {
        target.close()
    }
}

fun OutputTarget.asOutputStream(): OutputStream {
    return TargetOutputStream(this)
}
