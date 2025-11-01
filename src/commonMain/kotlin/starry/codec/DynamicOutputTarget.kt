package starry.codec

class DynamicOutputTarget : OutputTarget {

    private var closed = false
    private val list = mutableListOf<Byte>()

    override fun write(byte: Byte) {
        if (closed) {
            throw IllegalStateException("OutputTarget is closed")
        }
        list.add(byte)
    }

    fun toByteArray() = list.toByteArray()

    override fun close() {
        closed = true
    }

}
