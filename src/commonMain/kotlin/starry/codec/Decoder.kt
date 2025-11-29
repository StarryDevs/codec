package starry.codec

fun interface Decoder<T> {
    fun decode(input: InputSource): T
}
