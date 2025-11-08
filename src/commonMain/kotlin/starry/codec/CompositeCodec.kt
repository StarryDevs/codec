package starry.codec

private class CompositeCodec<T>(private val compositeCodecDsl: CompositeCodecDslImpl<T>) : Codec<T> {
    override fun decode(input: InputSource): T {
        val originValues = compositeCodecDsl.inputValues
        val newValues = mutableMapOf<CompositeCodecDsl.Dependency<*>, Any?>()
        for (dependency in compositeCodecDsl.dependencies) {
            try {
                val value = dependency.codec.decode(input)
                newValues[dependency] = value
            } catch (e: Exception) {
                throw IllegalStateException("Failed to decode dependency $dependency", e)
            }
        }
        compositeCodecDsl.inputValues = newValues
        val result = compositeCodecDsl.output()
        compositeCodecDsl.inputValues = originValues
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun encode(output: OutputTarget, value: T) {
        val originValues = compositeCodecDsl.outputValues
        compositeCodecDsl.outputValues = mutableMapOf()
        compositeCodecDsl.input(value)
        for (dependency in compositeCodecDsl.dependencies) {
            val outputValue = compositeCodecDsl.outputValues[dependency]
            if (outputValue == null) {
                if (dependency.codec is DefaultCodec<*>) {
                    dependency.codec.encodeDefault(output)
                } else {
                    throw IllegalStateException("Output value for dependency $dependency is null")
                }
            } else {
                (dependency as CompositeCodecDslImpl<T>.Dependency<Any?>).codec.encode(output, outputValue)
            }
        }
        compositeCodecDsl.outputValues = originValues
    }
}

interface CompositeCodecDsl<T> {
    interface Dependency<K> {
        operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): K
        operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: K)
    }

    fun output(block: () -> T)
    fun input(block: (T) -> Unit)

    operator fun <K> Codec<K>.unaryPlus(): Dependency<K>
}

private class CompositeCodecDslImpl<T> : CompositeCodecDsl<T> {
    inner class Dependency<K>(val codec: Codec<K>) : CompositeCodecDsl.Dependency<K> {
        @Suppress("UNCHECKED_CAST")
        override operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): K =
            inputValues[this] as K
        override operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: K) {
            outputValues[this] = value
        }

        override fun toString() = "Dependency[$codec]"
    }

    var dependencies = mutableListOf<Dependency<*>>()
    var inputValues: Map<CompositeCodecDsl.Dependency<*>, Any?> = emptyMap()
    var outputValues: MutableMap<CompositeCodecDsl.Dependency<*>, Any?> = mutableMapOf()

    lateinit var output: () -> T
    lateinit var input: (T) -> Unit

    override fun output(block: () -> T) {
        output = block
    }

    override fun input(block: (T) -> Unit) {
        input = block
    }

    override operator fun <K> Codec<K>.unaryPlus(): Dependency<K> = Dependency(this).also(dependencies::add)
}

fun <T> composite(block: CompositeCodecDsl<T>.() -> Unit): Codec<T> {
    val dsl = CompositeCodecDslImpl<T>()
    dsl.block()
    return CompositeCodec(dsl)
}
