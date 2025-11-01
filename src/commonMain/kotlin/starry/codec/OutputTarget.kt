package starry.codec

interface OutputTarget : AutoCloseable {
    companion object {
        fun wrap(byteArray: ByteArray, offset: Int = 0): OutputTarget = ArrayOutputTarget(byteArray, offset)
        fun allocate(size: Int): ArrayOutputTarget = ArrayOutputTarget(ByteArray(size))
        fun dynamic(): DynamicOutputTarget = DynamicOutputTarget()
    }

    fun write(byte: Byte)
}
