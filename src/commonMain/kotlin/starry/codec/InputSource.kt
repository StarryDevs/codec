package starry.codec

interface InputSource : Iterator<Byte>, AutoCloseable {

    companion object {
        fun wrap(byteArray: ByteArray) = ArrayInputSource(byteArray)
        fun empty(): InputSource = ArrayInputSource(byteArrayOf())
    }

    fun available(): Int
    override fun hasNext() = available() > 0

}
