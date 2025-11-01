package starry.codec

import kotlin.enums.enumEntries

private class PairCodec<A, B>(
    private val firstCodec: Codec<A>,
    private val secondCodec: Codec<B>
) : Codec<Pair<A, B>> {
    override fun decode(input: InputSource): Pair<A, B> {
        val first = firstCodec.decode(input)
        val second = secondCodec.decode(input)
        return Pair(first, second)
    }

    override fun encode(output: OutputTarget, value: Pair<A, B>) {
        firstCodec.encode(output, value.first)
        secondCodec.encode(output, value.second)
    }
}

infix fun <A, B> Codec<A>.with(other: Codec<B>): Codec<Pair<A, B>> =
    PairCodec(this, other)

private class NullableCodec<T>(private val codec: Codec<T>) : Codec<T?> {
    override fun decode(input: InputSource): T? {
        val isNotNull = input.next().toInt() != 0
        return if (isNotNull) {
            codec.decode(input)
        } else {
            null
        }
    }

    override fun encode(output: OutputTarget, value: T?) {
        if (value != null) {
            output.write(1.toByte())
            codec.encode(output, value)
        } else {
            output.write(0.toByte())
        }
    }
}

fun <T> Codec<T>.nullable(): Codec<T?> = NullableCodec(this)

private class ListCodec<T>(private val elementCodec: Codec<T>) : Codec<List<T>> {
    override fun decode(input: InputSource): List<T> {
        val size = IntCodec.decode(input)
        val list = ArrayList<T>(size)
        repeat(size) {
            list.add(elementCodec.decode(input))
        }
        return list
    }

    override fun encode(output: OutputTarget, value: List<T>) {
        IntCodec.encode(output, value.size)
        for (element in value) {
            elementCodec.encode(output, element)
        }
    }
}

fun <T> Codec<T>.list(): Codec<List<T>> = ListCodec(this)

private class CollectionCodec<T>(private val elementCodec: Codec<T>, private val size: Int) : Codec<Collection<T>> {

    override fun decode(input: InputSource): Collection<T> {
        val collection = ArrayList<T>(size)
        repeat(size) {
            collection.add(elementCodec.decode(input))
        }
        return collection
    }

    override fun encode(output: OutputTarget, value: Collection<T>) {
        for (element in value) {
            elementCodec.encode(output, element)
        }
    }

}

fun <T> Codec<T>.collection(size: Int): Codec<Collection<T>> = CollectionCodec(this, size)

private class MapCodec<K, V>(private val keyCodec: Codec<K>, private val valueCodec: Codec<V>) : Codec<Map<K, V>> {
    override fun decode(input: InputSource): Map<K, V> {
        val size = IntCodec.decode(input)
        val map = LinkedHashMap<K, V>(size)
        repeat(size) {
            val key = keyCodec.decode(input)
            val value = valueCodec.decode(input)
            map[key] = value
        }
        return map
    }

    override fun encode(output: OutputTarget, value: Map<K, V>) {
        IntCodec.encode(output, value.size)
        for ((key, v) in value) {
            keyCodec.encode(output, key)
            valueCodec.encode(output, v)
        }
    }
}

infix fun <K, V> Codec<K>.associate(valueCodec: Codec<V>): Codec<Map<K, V>> = MapCodec(this, valueCodec)

private class MappingCodec<I, O>(
    private val baseCodec: Codec<I>,
    private val toOutput: (I) -> O,
    private val fromOutput: (O) -> I
) : Codec<O> {
    override fun decode(input: InputSource): O {
        val baseValue = baseCodec.decode(input)
        return toOutput(baseValue)
    }

    override fun encode(output: OutputTarget, value: O) {
        val baseValue = fromOutput(value)
        baseCodec.encode(output, baseValue)
    }
}

fun <I, O> Codec<I>.map(toOutput: (I) -> O, fromOutput: (O) -> I): Codec<O> =
    MappingCodec(this, toOutput, fromOutput)

class NoopCodec(val size: Int) : Codec<Unit>, DefaultCodec<Unit> {
    override fun decode(input: InputSource) {
        repeat(size) {
            input.next()
        }
    }

    override fun encodeDefault(output: OutputTarget) {
        repeat(size) {
            output.write(0.toByte())
        }
    }

    override fun encode(output: OutputTarget, value: Unit) = encodeDefault(output)
}

private class UnionCodec<T>(list: List<T>) : Codec<T> {
    private val valueToIndex: Map<T, Int> = list.withIndex().associate { it.value to it.index }
    private val indexToValue: Map<Int, T> = list.withIndex().associate { it.index to it.value }

    override fun encode(output: OutputTarget, value: T) {
        val index = valueToIndex[value]
            ?: throw IllegalArgumentException("Value $value is not in the union")
        IntCodec.encode(output, index)
    }

    override fun decode(input: InputSource): T {
        val index = IntCodec.decode(input)
        return indexToValue[index]
            ?: throw IllegalArgumentException("Index $index is not in the union")
    }
}

@Suppress("FunctionName")
fun <T> UnionCodec(vararg items: T): Codec<T> = UnionCodec(items.toList())

@Suppress("FunctionName")
inline fun <reified T : Enum<T>> EnumCodec(): Codec<T> {
    val entries = enumEntries<T>()
    return UnionCodec(*entries.toTypedArray())
}
