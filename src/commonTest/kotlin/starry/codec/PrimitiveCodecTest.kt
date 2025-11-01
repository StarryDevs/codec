package starry.codec

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimitiveCodecTest {

    @Test
    fun testLong() {
        val target = OutputTarget.dynamic()
        LongCodec.encode(target, Long.MAX_VALUE)
        val source = InputSource.wrap(target.toByteArray())
        val result = LongCodec.decode(source)
        assertEquals(Long.MAX_VALUE, result)
    }

    @Test
    fun testString() {
        val codec = StringCodec
        val original = "Hello, world!"
        val target = OutputTarget.dynamic()
        codec.encode(target, original)
        println(target.toByteArray().contentToString())
        val source = InputSource.wrap(target.toByteArray())
        val result = codec.decode(source)
        assertEquals(original, result)
    }

}
