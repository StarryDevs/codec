package starry.codec.integration.arrow

import arrow.core.Either
import starry.codec.Codec
import starry.codec.InputSource
import starry.codec.OutputTarget

class EitherCodec<A, B>(val codecA: Codec<A>, val codecB: Codec<B>) : Codec<Either<A, B>> {

    override fun decode(input: InputSource): Either<A, B> {
        val flag = input.next().toInt()
        return when (flag) {
            0 -> Either.Left(codecA.decode(input))
            1 -> Either.Right(codecB.decode(input))
            else -> throw IllegalStateException("Invalid flag for Either: $flag")
        }
    }

    override fun encode(output: OutputTarget, value: Either<A, B>) {
        when (value) {
            is Either.Left -> {
                output.write(0)
                codecA.encode(output, value.value)
            }

            is Either.Right -> {
                output.write(1)
                codecB.encode(output, value.value)
            }
        }
    }

}

infix fun <A, B> Codec<A>.or(other: Codec<B>): Codec<Either<A, B>> = EitherCodec(this, other)
