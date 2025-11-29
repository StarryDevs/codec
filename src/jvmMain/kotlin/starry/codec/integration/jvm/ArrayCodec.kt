package starry.codec.integration.jvm

import starry.codec.Codec
import starry.codec.InputSource
import starry.codec.IntCodec
import starry.codec.OutputTarget
import kotlin.reflect.KClass

class ArrayCodec<T>(val elementCodec: Codec<T>, val elementType: KClass<*>) : Codec<Array<T>> {

    @Suppress("UNCHECKED_CAST")
    override fun decode(input: InputSource): Array<T> {
        val size = IntCodec.decode(input)
        val array = java.lang.reflect.Array.newInstance(elementType.java, size) as Array<T>
        for (i in 0 until size) {
            array[i] = elementCodec.decode(input)
        }
        return array
    }

    override fun encode(output: OutputTarget, value: Array<T>) {
        IntCodec.encode(output, value.size)
        for (element in value) {
            elementCodec.encode(output, element)
        }
    }

}

inline fun <reified T> Codec<T>.array(): Codec<Array<T>> {
    return ArrayCodec(this, T::class)
}
