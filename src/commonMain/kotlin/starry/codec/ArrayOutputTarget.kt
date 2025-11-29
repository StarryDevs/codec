package starry.codec

class ArrayOutputTarget internal constructor(private var array: ByteArray = byteArrayOf(), offset: Int = 0) :
    OutputTarget {

    private var closed: Boolean = false
    private var position: Int = offset

    override fun write(byte: Byte) {
        if (closed) {
            throw IllegalStateException("OutputTarget is closed")
        }
        if (position >= array.size) {
            throw IndexOutOfBoundsException("Cannot write beyond the end of the array")
        }
        array[position] = byte
        position++
    }

    fun toByteArray() = array

    override fun close() {
        closed = true
    }

}
