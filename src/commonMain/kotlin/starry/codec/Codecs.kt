package starry.codec

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.enums.enumEntries
import kotlin.time.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private class EncoderDecoderCodec<T>(
    private val encoder: Encoder<T>,
    private val decoder: Decoder<T>
) : Codec<T> {
    override fun decode(input: InputSource): T = decoder.decode(input)
    override fun encode(output: OutputTarget, value: T) = encoder.encode(output, value)
}

/**
 * 将 [Encoder] 和 [Decoder] 组合成 [Codec]
 */
infix fun <T> Encoder<T>.with(decoder: Decoder<T>): Codec<T> = EncoderDecoderCodec(this, decoder)

/**
 * 将 [Decoder] 和 [Encoder] 组合成 [Codec]
 */
infix fun <T> Decoder<T>.with(encoder: Encoder<T>): Codec<T> = EncoderDecoderCodec(encoder, this)

/**
 * [Int] 编解码器
 */
object IntCodec : Codec<Int>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0)

    override fun decode(input: InputSource): Int {
        var result = 0
        for (i in 0 until 4) {
            result = (result shl 8) or (input.next().toInt() and 0xFF)
        }
        return result
    }

    override fun encode(output: OutputTarget, value: Int) {
        for (i in 3 downTo 0) {
            output.write(((value shr (i * 8)) and 0xFF).toByte())
        }
    }
}

/**
 * [Byte] 编解码器
 */
object ByteCodec : Codec<Byte>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0)

    override fun decode(input: InputSource): Byte {
        return input.next()
    }

    override fun encode(output: OutputTarget, value: Byte) {
        output.write(value)
    }
}

/**
 * [Short] 编解码器
 */
object ShortCodec : Codec<Short>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0)

    override fun decode(input: InputSource): Short {
        var result = 0
        for (i in 0 until 2) {
            result = (result shl 8) or (input.next().toInt() and 0xFF)
        }
        return result.toShort()
    }

    override fun encode(output: OutputTarget, value: Short) {
        for (i in 1 downTo 0) {
            output.write(((value.toInt() shr (i * 8)) and 0xFF).toByte())
        }
    }
}

/**
 * [Long] 编解码器
 */
object LongCodec : Codec<Long>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0)

    override fun decode(input: InputSource): Long {
        var result = 0L
        for (i in 0 until 8) {
            result = (result shl 8) or (input.next().toLong() and 0xFF)
        }
        return result
    }

    override fun encode(output: OutputTarget, value: Long) {
        for (i in 7 downTo 0) {
            output.write(((value shr (i * 8)) and 0xFF).toByte())
        }
    }
}

/**
 * [Char] 编解码器
 */
object CharCodec : Codec<Char>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0.toChar())

    override fun decode(input: InputSource): Char {
        var result = 0
        for (i in 0 until 2) {
            result = (result shl 8) or (input.next().toInt() and 0xFF)
        }
        return result.toChar()
    }

    override fun encode(output: OutputTarget, value: Char) {
        for (i in 1 downTo 0) {
            output.write(((value.code shr (i * 8)) and 0xFF).toByte())
        }
    }
}

/**
 * [Boolean] 编解码器
 */
object BooleanCodec : Codec<Boolean>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, false)

    override fun decode(input: InputSource): Boolean {
        return input.next().toInt() != 0
    }

    override fun encode(output: OutputTarget, value: Boolean) {
        output.write(if (value) 1.toByte() else 0.toByte())
    }
}

/**
 * [Float] 编解码器
 */
object FloatCodec : Codec<Float>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0F)

    override fun decode(input: InputSource): Float {
        val intBits = IntCodec.decode(input)
        return Float.fromBits(intBits)
    }

    override fun encode(output: OutputTarget, value: Float) {
        val intBits = value.toBits()
        IntCodec.encode(output, intBits)
    }
}

/**
 * [Double] 编解码器
 */
object DoubleCodec : Codec<Double>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0.0)

    override fun decode(input: InputSource): Double {
        val longBits = LongCodec.decode(input)
        return Double.fromBits(longBits)
    }

    override fun encode(output: OutputTarget, value: Double) {
        val longBits = value.toBits()
        LongCodec.encode(output, longBits)
    }
}

/**
 * [UInt] 编解码器
 */
object UIntCodec : Codec<UInt>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0.toUInt())

    override fun decode(input: InputSource): UInt {
        return IntCodec.decode(input).toUInt()
    }

    override fun encode(output: OutputTarget, value: UInt) {
        IntCodec.encode(output, value.toInt())
    }
}

/**
 * [ULong] 编解码器
 */
object ULongCodec : Codec<ULong>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0.toULong())

    override fun decode(input: InputSource): ULong {
        return LongCodec.decode(input).toULong()
    }

    override fun encode(output: OutputTarget, value: ULong) {
        LongCodec.encode(output, value.toLong())
    }
}

/**
 * [UByte] 编解码器
 */
object UByteCodec : Codec<UByte>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0.toUByte())

    override fun decode(input: InputSource): UByte {
        return ByteCodec.decode(input).toUByte()
    }

    override fun encode(output: OutputTarget, value: UByte) {
        ByteCodec.encode(output, value.toByte())
    }
}

/**
 * [UShort] 编解码器
 */
object UShortCodec : Codec<UShort>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, 0.toUShort())

    override fun decode(input: InputSource): UShort {
        return ShortCodec.decode(input).toUShort()
    }

    override fun encode(output: OutputTarget, value: UShort) {
        ShortCodec.encode(output, value.toShort())
    }
}

/**
 * [Unit] 编解码器
 */
object UnitCodec : Codec<Unit>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, Unit)
    override fun decode(input: InputSource) {}
    override fun encode(output: OutputTarget, value: Unit) {}
}

/**
 * [ByteArray] 编解码器
 */
object ByteArrayCodec : Codec<ByteArray>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, ByteArray(0))

    override fun decode(input: InputSource) =
        ByteArray(IntCodec.decode(input)) { ByteCodec.decode(input) }

    override fun encode(output: OutputTarget, value: ByteArray) {
        IntCodec.encode(output, value.size)
        for (byte in value) {
            ByteCodec.encode(output, byte)
        }
    }
}

/**
 * [String] 编解码器
 */
object StringCodec : Codec<String>, DefaultEncoder {
    override fun encodeDefault(output: OutputTarget) = encode(output, "")
    override fun decode(input: InputSource): String {
        val chars = CharArray(IntCodec.decode(input)) { CharCodec.decode(input) }
        return chars.concatToString()
    }

    override fun encode(output: OutputTarget, value: String) {
        IntCodec.encode(output, value.length)
        for (char in value) {
            CharCodec.encode(output, char)
        }
    }
}

/**
 * [LocalDate] 编解码器
 */
object LocalDateCodec : Codec<LocalDate> {
    override fun decode(input: InputSource): LocalDate {
        val epochDays = LongCodec.decode(input)
        return LocalDate.fromEpochDays(epochDays)
    }

    override fun encode(output: OutputTarget, value: LocalDate) {
        val epochDays = value.toEpochDays()
        LongCodec.encode(output, epochDays)
    }
}

/**
 * [LocalDateRange] 编解码器
 */
object LocalDateRangeCodec : Codec<LocalDateRange> {
    override fun decode(input: InputSource): LocalDateRange {
        val start = LocalDateCodec.decode(input)
        val endInclusive = LocalDateCodec.decode(input)
        return LocalDateRange(start, endInclusive)
    }

    override fun encode(output: OutputTarget, value: LocalDateRange) {
        LocalDateCodec.encode(output, value.start)
        LocalDateCodec.encode(output, value.endInclusive)
    }
}

/**
 * [LocalTime] 编解码器
 */
object LocalTimeCodec : Codec<LocalTime> {
    override fun decode(input: InputSource): LocalTime {
        val nanosecondsSinceMidnight = LongCodec.decode(input)
        return LocalTime.fromNanosecondOfDay(nanosecondsSinceMidnight)
    }

    override fun encode(output: OutputTarget, value: LocalTime) {
        val nanosecondsSinceMidnight = value.toNanosecondOfDay()
        LongCodec.encode(output, nanosecondsSinceMidnight)
    }
}

/**
 * [LocalDateTime] 编解码器
 */
object LocalDateTimeCodec : Codec<LocalDateTime> {
    override fun decode(input: InputSource): LocalDateTime {
        val date = LocalDateCodec.decode(input)
        val time = LocalTimeCodec.decode(input)
        return LocalDateTime(date, time)
    }

    override fun encode(output: OutputTarget, value: LocalDateTime) {
        LocalDateCodec.encode(output, value.date)
        LocalTimeCodec.encode(output, value.time)
    }
}

/**
 * [Instant] 编解码器
 */
@OptIn(ExperimentalTime::class)
object InstantCodec : Codec<Instant> {
    override fun decode(input: InputSource): Instant {
        val epochNanoseconds = LongCodec.decode(input)
        return Instant.fromEpochMilliseconds(epochNanoseconds)
    }

    override fun encode(output: OutputTarget, value: Instant) {
        val epochNanoseconds = value.toEpochMilliseconds()
        LongCodec.encode(output, epochNanoseconds)
    }
}

/**
 * [Duration] 编解码器
 */
object DurationCodec : Codec<Duration> {
    override fun decode(input: InputSource): Duration {
        val longBits = LongCodec.decode(input)
        return longBits.toDuration(DurationUnit.NANOSECONDS)
    }

    override fun encode(output: OutputTarget, value: Duration) {
        val longBits = value.inWholeNanoseconds
        LongCodec.encode(output, longBits)
    }
}

/**
 * [Uuid] 编解码器
 */
@OptIn(ExperimentalUuidApi::class)
object UuidCodec : Codec<Uuid> {
    override fun decode(input: InputSource): Uuid {
        val mostSigBits = LongCodec.decode(input)
        val leastSigBits = LongCodec.decode(input)
        return Uuid.fromLongs(mostSigBits, leastSigBits)
    }

    override fun encode(output: OutputTarget, value: Uuid) {
        val (mostSigBits, leastSigBits) = value.toULongs(::Pair)
        LongCodec.encode(output, mostSigBits.toLong())
        LongCodec.encode(output, leastSigBits.toLong())
    }
}

/**
 * [IntRange] 编解码器
 */
object IntRangeCodec : Codec<IntRange> {
    override fun decode(input: InputSource): IntRange {
        val start = IntCodec.decode(input)
        val endInclusive = IntCodec.decode(input)
        return IntRange(start, endInclusive)
    }

    override fun encode(output: OutputTarget, value: IntRange) {
        IntCodec.encode(output, value.first)
        IntCodec.encode(output, value.last)
    }
}

/**
 * [IntProgression] 编解码器
 */
object IntProgressionCodec : Codec<IntProgression> {
    override fun decode(input: InputSource): IntProgression {
        val start = IntCodec.decode(input)
        val endInclusive = IntCodec.decode(input)
        val step = IntCodec.decode(input)
        return IntProgression.fromClosedRange(start, endInclusive, step)
    }

    override fun encode(output: OutputTarget, value: IntProgression) {
        IntCodec.encode(output, value.first)
        IntCodec.encode(output, value.last)
        IntCodec.encode(output, value.step)
    }
}

/**
 * [LongRange] 编解码器
 */
object LongRangeCodec : Codec<LongRange> {
    override fun decode(input: InputSource): LongRange {
        val start = LongCodec.decode(input)
        val endInclusive = LongCodec.decode(input)
        return LongRange(start, endInclusive)
    }

    override fun encode(output: OutputTarget, value: LongRange) {
        LongCodec.encode(output, value.first)
        LongCodec.encode(output, value.last)
    }
}

/**
 * [LongProgression] 编解码器
 */
object LongProgressionCodec : Codec<LongProgression> {
    override fun decode(input: InputSource): LongProgression {
        val start = LongCodec.decode(input)
        val endInclusive = LongCodec.decode(input)
        val step = LongCodec.decode(input)
        return LongProgression.fromClosedRange(start, endInclusive, step)
    }

    override fun encode(output: OutputTarget, value: LongProgression) {
        LongCodec.encode(output, value.first)
        LongCodec.encode(output, value.last)
        LongCodec.encode(output, value.step)
    }
}

/**
 * [CharRange] 编解码器
 */
object CharRangeCodec : Codec<CharRange> {
    override fun decode(input: InputSource): CharRange {
        val start = CharCodec.decode(input)
        val endInclusive = CharCodec.decode(input)
        return CharRange(start, endInclusive)
    }

    override fun encode(output: OutputTarget, value: CharRange) {
        CharCodec.encode(output, value.first)
        CharCodec.encode(output, value.last)
    }
}

/**
 * [CharProgression] 编解码器
 */
object CharProgressionCodec : Codec<CharProgression> {
    override fun decode(input: InputSource): CharProgression {
        val start = CharCodec.decode(input)
        val endInclusive = CharCodec.decode(input)
        val step = IntCodec.decode(input)
        return CharProgression.fromClosedRange(start, endInclusive, step)
    }

    override fun encode(output: OutputTarget, value: CharProgression) {
        CharCodec.encode(output, value.first)
        CharCodec.encode(output, value.last)
        IntCodec.encode(output, value.step)
    }
}

/**
 * [UIntRange] 编解码器
 */
object UIntRangeCodec : Codec<UIntRange> {
    override fun decode(input: InputSource): UIntRange {
        val start = UIntCodec.decode(input)
        val endInclusive = UIntCodec.decode(input)
        return UIntRange(start, endInclusive)
    }

    override fun encode(output: OutputTarget, value: UIntRange) {
        UIntCodec.encode(output, value.first)
        UIntCodec.encode(output, value.last)
    }
}

/**
 * [UIntProgression] 编解码器
 */
object UIntProgressionCodec : Codec<UIntProgression> {
    override fun decode(input: InputSource): UIntProgression {
        val start = UIntCodec.decode(input)
        val endInclusive = UIntCodec.decode(input)
        val step = IntCodec.decode(input)
        return UIntProgression.fromClosedRange(start, endInclusive, step)
    }

    override fun encode(output: OutputTarget, value: UIntProgression) {
        UIntCodec.encode(output, value.first)
        UIntCodec.encode(output, value.last)
        IntCodec.encode(output, value.step)
    }
}

/**
 * [ULongRange] 编解码器
 */
object ULongRangeCodec : Codec<ULongRange> {
    override fun decode(input: InputSource): ULongRange {
        val start = ULongCodec.decode(input)
        val endInclusive = ULongCodec.decode(input)
        return ULongRange(start, endInclusive)
    }

    override fun encode(output: OutputTarget, value: ULongRange) {
        ULongCodec.encode(output, value.first)
        ULongCodec.encode(output, value.last)
    }
}

/**
 * [ULongProgression] 编解码器
 */
object ULongProgressionCodec : Codec<ULongProgression> {
    override fun decode(input: InputSource): ULongProgression {
        val start = ULongCodec.decode(input)
        val endInclusive = ULongCodec.decode(input)
        val step = LongCodec.decode(input)
        return ULongProgression.fromClosedRange(start, endInclusive, step)
    }

    override fun encode(output: OutputTarget, value: ULongProgression) {
        ULongCodec.encode(output, value.first)
        ULongCodec.encode(output, value.last)
        LongCodec.encode(output, value.step)
    }
}

private class ClosedRangeCodec<T : Comparable<T>>(val codec: Codec<T>) : Codec<ClosedRange<T>> {
    override fun decode(input: InputSource): ClosedRange<T> {
        val start = codec.decode(input)
        val endInclusive = codec.decode(input)
        return start.rangeTo(endInclusive)
    }

    override fun encode(output: OutputTarget, value: ClosedRange<T>) {
        codec.encode(output, value.start)
        codec.encode(output, value.endInclusive)
    }
}

/**
 * [ClosedRange] 编解码器
 */
fun <T : Comparable<T>> Codec<T>.closedRange(): Codec<ClosedRange<T>> =
    ClosedRangeCodec(this)

private class OpenEndRangeCodec<T : Comparable<T>>(val codec: Codec<T>) : Codec<OpenEndRange<T>> {
    override fun decode(input: InputSource): OpenEndRange<T> {
        val start = codec.decode(input)
        val endExclusive = codec.decode(input)
        return start.rangeUntil(endExclusive)
    }

    override fun encode(output: OutputTarget, value: OpenEndRange<T>) {
        codec.encode(output, value.start)
        codec.encode(output, value.endExclusive)
    }
}

/**
 * [OpenEndRange] 编解码器
 */
fun <T : Comparable<T>> Codec<T>.openEndRange(): Codec<OpenEndRange<T>> =
    OpenEndRangeCodec(this)

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

/**
 * [Pair] 编解码器
 */
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

/**
 * 可空类型编解码器
 */
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

/**
 * [List] 编解码器
 */
fun <T> Codec<T>.list(): Codec<List<T>> = ListCodec(this)

/**
 * [Set] 编解码器
 */
fun <T> Codec<T>.set(): Codec<Set<T>> = list().map({ it.toSet() }, { it.toList() })

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

/**
 * 固定大小的 [Collection] 编解码器
 */
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

/**
 * [Map] 编解码器
 */
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

/**
 * 映射编解码器
 */
fun <I, O> Codec<I>.map(toOutput: (I) -> O, fromOutput: (O) -> I): Codec<O> =
    MappingCodec(this, toOutput, fromOutput)

/**
 * 无操作编解码器
 */
class NoopCodec(val size: Int = 0) : Codec<Unit> {
    companion object : Codec<Unit> by NoopCodec(0)

    override fun decode(input: InputSource) {
        repeat(size) {
            input.next()
        }

    }

    override fun encode(output: OutputTarget, value: Unit) {
        repeat(size) {
            output.write(0.toByte())
        }
    }
}

private class UnionCodec<T>(list: List<T>) : Codec<T> {
    private val indexCodec: Codec<Int> = object : Codec<Int> {
        private val maxIndex = (list.size - 1).coerceAtLeast(0)
        private val bytes: Int = when {
            maxIndex == 0 -> 0
            maxIndex <= 0xFF -> 1
            maxIndex <= 0xFFFF -> 2
            maxIndex <= 0xFFFFFF -> 3
            else -> 4
        }

        override fun decode(input: InputSource): Int {
            var result = 0
            repeat(bytes) {
                result = (result shl 8) or (input.next().toInt() and 0xFF)
            }
            return result
        }

        override fun encode(output: OutputTarget, value: Int) {
            if (value < 0 || value > maxIndex) {
                throw IllegalArgumentException("Index $value out of range 0..$maxIndex")
            }
            for (i in bytes - 1 downTo 0) {
                output.write(((value shr (i * 8)) and 0xFF).toByte())
            }
        }
    }

    private val valueToIndex: Map<T, Int> = list.withIndex().associate { it.value to it.index }
    private val indexToValue: Map<Int, T> = list.withIndex().associate { it.index to it.value }

    @Suppress("USELESS_CAST")
    override fun encode(output: OutputTarget, value: T) {
        val index = valueToIndex[value]
            ?: throw IllegalArgumentException("Value $value is not in the union")
        indexCodec.encode(output, index)
    }

    override fun decode(input: InputSource): T {
        val index = indexCodec.decode(input)
        return indexToValue[index]
            ?: throw IllegalArgumentException("Index $index is not in the union")
    }
}

/**
 * 联合编解码器
 */
@Suppress("FunctionName")
fun <T> UnionCodec(vararg items: T): Codec<T> {
    if (items.isEmpty()) {
        throw IllegalArgumentException("UnionCodec requires at least one item")
    }
    if (items.size == 1) {
        val singleton = items.single()
        return NoopCodec.map({ items[0] }) {
            if (it != singleton) {
                throw IllegalArgumentException("Value $it is not in the union")
            }
        }
    }
    return UnionCodec(items.toList())
}

/**
 * 枚举编解码器
 */
@Suppress("FunctionName")
inline fun <reified T : Enum<T>> EnumCodec(): Codec<T> {
    val entries = enumEntries<T>()
    return UnionCodec(*entries.toTypedArray())
}
