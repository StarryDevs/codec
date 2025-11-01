package starry.codec.integration.arrow

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import starry.codec.Codec
import starry.codec.OutputTarget

class OptionCodec<T>(val codec: Codec<T>) : Codec<Option<T>> {

    override fun encode(output: OutputTarget, value: Option<T>) {
        when (value) {
            is Some -> {
                output.write(1)
                codec.encode(output, value.value)
            }
            is None -> {
                output.write(0)
            }
        }
    }

    override fun decode(input: starry.codec.InputSource): Option<T> {
        val flag = input.next().toInt()
        return when (flag) {
            1 -> Some(codec.decode(input))
            0 -> None
            else -> throw IllegalStateException("Invalid flag for Option: $flag")
        }
    }

}

fun <T> Codec<T>.optional(): Codec<Option<T>> = OptionCodec(this)
