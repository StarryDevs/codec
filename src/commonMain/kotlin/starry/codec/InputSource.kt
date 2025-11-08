package starry.codec

interface InputSource : Iterator<Byte>, AutoCloseable {

    companion object {
        fun wrap(byteArray: ByteArray) = ArrayInputSource(byteArray)
        fun empty(): InputSource = ArrayInputSource(byteArrayOf())
    }

    fun available(): Int
    override fun hasNext() = available() > 0

    fun skip(n: Long): Long {
        var skipped = 0L
        while (skipped < n && hasNext()) {
            next()
            skipped++
        }
        return skipped
    }

}
