package starry.codec.integration.arrow

import arrow.core.Ior
import starry.codec.Codec
import starry.codec.InputSource
import starry.codec.OutputTarget

class IorCodec<A, B>(val leftCodec: Codec<A>, val rightCodec: Codec<B>) : Codec<Ior<A, B>> {

    override fun encode(output: OutputTarget, value: Ior<A, B>) {
        when (value) {
            is Ior.Left -> {
                output.write(0)
                leftCodec.encode(output, value.value)
            }

            is Ior.Right -> {
                output.write(1)
                rightCodec.encode(output, value.value)
            }

            is Ior.Both -> {
                output.write(2)
                leftCodec.encode(output, value.leftValue)
                rightCodec.encode(output, value.rightValue)
            }
        }
    }

    override fun decode(input: InputSource): Ior<A, B> {
        return when (input.next().toInt()) {
            0 -> Ior.Left(leftCodec.decode(input))
            1 -> Ior.Right(rightCodec.decode(input))
            2 -> Ior.Both(leftCodec.decode(input), rightCodec.decode(input))
            else -> throw IllegalStateException("Invalid flag for Ior: ${input.next()}")
        }
    }

}
