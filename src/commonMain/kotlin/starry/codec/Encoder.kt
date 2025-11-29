package starry.codec

fun interface Encoder<T> {
    fun encode(output: OutputTarget, value: T)
}
