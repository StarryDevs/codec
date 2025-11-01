package starry.codec

object IntCodec : Codec<Int> {
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

object ByteCodec : Codec<Byte> {
    override fun decode(input: InputSource): Byte {
        return input.next()
    }

    override fun encode(output: OutputTarget, value: Byte) {
        output.write(value)
    }
}

object ShortCodec : Codec<Short> {
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

object LongCodec : Codec<Long> {
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

object CharCodec : Codec<Char> {
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

object BooleanCodec : Codec<Boolean> {
    override fun decode(input: InputSource): Boolean {
        return input.next().toInt() != 0
    }

    override fun encode(output: OutputTarget, value: Boolean) {
        output.write(if (value) 1.toByte() else 0.toByte())
    }
}

object FloatCodec : Codec<Float> {
    override fun decode(input: InputSource): Float {
        val intBits = IntCodec.decode(input)
        return Float.fromBits(intBits)
    }

    override fun encode(output: OutputTarget, value: Float) {
        val intBits = value.toBits()
        IntCodec.encode(output, intBits)
    }
}

object DoubleCodec : Codec<Double> {
    override fun decode(input: InputSource): Double {
        val longBits = LongCodec.decode(input)
        return Double.fromBits(longBits)
    }

    override fun encode(output: OutputTarget, value: Double) {
        val longBits = value.toBits()
        LongCodec.encode(output, longBits)
    }
}

class UIntCodec : Codec<UInt> {
    override fun decode(input: InputSource): UInt {
        return IntCodec.decode(input).toUInt()
    }

    override fun encode(output: OutputTarget, value: UInt) {
        IntCodec.encode(output, value.toInt())
    }
}

class ULongCodec : Codec<ULong> {
    override fun decode(input: InputSource): ULong {
        return LongCodec.decode(input).toULong()
    }

    override fun encode(output: OutputTarget, value: ULong) {
        LongCodec.encode(output, value.toLong())
    }
}

class UByteCodec : Codec<UByte> {
    override fun decode(input: InputSource): UByte {
        return ByteCodec.decode(input).toUByte()
    }

    override fun encode(output: OutputTarget, value: UByte) {
        ByteCodec.encode(output, value.toByte())
    }
}

class UShortCodec : Codec<UShort> {
    override fun decode(input: InputSource): UShort {
        return ShortCodec.decode(input).toUShort()
    }

    override fun encode(output: OutputTarget, value: UShort) {
        ShortCodec.encode(output, value.toShort())
    }
}

object UnitCodec : Codec<Unit> {
    override fun decode(input: InputSource) {}
    override fun encode(output: OutputTarget, value: Unit) {}
}

object ByteArrayCodec : Codec<ByteArray> {
    override fun decode(input: InputSource) =
        ByteArray(IntCodec.decode(input)) { ByteCodec.decode(input) }

    override fun encode(output: OutputTarget, value: ByteArray) {
        IntCodec.encode(output, value.size)
        for (byte in value) {
            ByteCodec.encode(output, byte)
        }
    }
}

object StringCodec : Codec<String> {
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
