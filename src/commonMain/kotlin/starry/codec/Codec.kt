package starry.codec

interface Codec<T> {
    fun encode(output: OutputTarget, value: T)
    fun decode(input: InputSource): T
}

interface DefaultCodec<T> {
    fun encodeDefault(output: OutputTarget)
}
