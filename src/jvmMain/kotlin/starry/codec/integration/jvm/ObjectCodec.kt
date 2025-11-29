package starry.codec.integration.jvm

import starry.codec.Codec
import starry.codec.InputSource
import starry.codec.IntCodec
import starry.codec.OutputTarget
import java.io.*

@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class UnsafeObjectSerializationApi

@UnsafeObjectSerializationApi
private object ObjectCodec : Codec<Any> {

    @Suppress("UNCHECKED_CAST")
    override fun decode(input: InputSource): Any {
        val size = IntCodec.decode(input)
        val read = ByteArray(size) { input.next() }
        return ByteArrayInputStream(read).use { byteArrayInputStream ->
            ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                objectInputStream.readObject()
            }
        }
    }

    override fun encode(output: OutputTarget, value: Any) {
        val bytes = ByteArrayOutputStream().use { byteArrayOutputStream ->
            ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                objectOutputStream.writeObject(value)
                objectOutputStream.flush()
            }
            byteArrayOutputStream.toByteArray()
        }
        IntCodec.encode(output, bytes.size)
        bytes.forEach(output::write)
    }

}

/**
 * 利用 Java 内置对象序列化机制的 Codec
 */
@UnsafeObjectSerializationApi
@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T> ObjectCodec(): Codec<T> = ObjectCodec as Codec<T>
