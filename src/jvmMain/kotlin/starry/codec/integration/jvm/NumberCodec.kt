package starry.codec.integration.jvm

import starry.codec.ByteArrayCodec
import starry.codec.Codec
import starry.codec.InputSource
import starry.codec.IntCodec
import starry.codec.OutputTarget
import java.math.BigDecimal
import java.math.BigInteger

object BigIntCodec : Codec<BigInteger> {

    override fun decode(input: InputSource) = BigInteger(ByteArrayCodec.decode(input))
    override fun encode(output: OutputTarget, value: BigInteger) = ByteArrayCodec.encode(output, value.toByteArray())

}

object BigDecimalCodec : Codec<BigDecimal> {

    override fun decode(input: InputSource): BigDecimal {
        val unscaledValue = BigIntCodec.decode(input)
        val scale = IntCodec.decode(input)
        return BigDecimal(unscaledValue, scale)
    }

    override fun encode(output: OutputTarget, value: BigDecimal) {
        BigIntCodec.encode(output, value.unscaledValue())
        IntCodec.encode(output, value.scale())
    }

}
