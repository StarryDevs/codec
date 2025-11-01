package starry.codec

class ArrayInputSource internal constructor(private var all: ByteArray) : InputSource {

    private var closed: Boolean = false
    private var position: Int = 0

    override fun next(): Byte {
        if (closed) {
            throw IllegalStateException("InputSource is closed")
        }
        if (position >= all.size) {
            throw IndexOutOfBoundsException("No more bytes available in InputSource.")
        }
        return all[position++]
    }

    override fun available() = all.size - position

    override fun close() {
        closed = true
    }

}

