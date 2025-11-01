package starry.codec

private class ComposeCodec<T>(private val composeCodecDsl: ComposeCodecDslImpl<T>) : Codec<T> {
    override fun decode(input: InputSource): T {
        val originValues = composeCodecDsl.inputValues
        val newValues = mutableMapOf<ComposeCodecDsl.Dependency<*>, Any?>()
        for (dependency in composeCodecDsl.dependencies) {
            try {
                val value = dependency.codec.decode(input)
                newValues[dependency] = value
            } catch (e: Exception) {
                throw IllegalStateException("Failed to decode dependency $dependency", e)
            }
        }
        composeCodecDsl.inputValues = newValues
        val result = composeCodecDsl.output()
        composeCodecDsl.inputValues = originValues
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun encode(output: OutputTarget, value: T) {
        val originValues = composeCodecDsl.outputValues
        composeCodecDsl.outputValues = mutableMapOf()
        composeCodecDsl.input(value)
        for (dependency in composeCodecDsl.dependencies) {
            val outputValue = composeCodecDsl.outputValues[dependency]
            if (outputValue == null) {
                if (dependency.codec is DefaultCodec<*>) {
                    dependency.codec.encodeDefault(output)
                } else {
                    throw IllegalStateException("Output value for dependency $dependency is null")
                }
            } else {
                (dependency as ComposeCodecDslImpl<T>.Dependency<Any?>).codec.encode(output, outputValue)
            }
        }
        composeCodecDsl.outputValues = originValues
    }
}

interface ComposeCodecDsl<T> {
    interface Dependency<K> {
        operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): K
        operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: K)
    }

    fun output(block: () -> T)
    fun input(block: (T) -> Unit)

    operator fun <K> Codec<K>.unaryPlus(): Dependency<K>
}

private class ComposeCodecDslImpl<T> : ComposeCodecDsl<T> {
    inner class Dependency<K>(val codec: Codec<K>) : ComposeCodecDsl.Dependency<K> {
        @Suppress("UNCHECKED_CAST")
        override operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): K =
            inputValues[this] as K
        override operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: K) {
            outputValues[this] = value
        }

        override fun toString() = "Dependency[$codec]"
    }

    var dependencies = mutableListOf<Dependency<*>>()
    var inputValues: Map<ComposeCodecDsl.Dependency<*>, Any?> = emptyMap()
    var outputValues: MutableMap<ComposeCodecDsl.Dependency<*>, Any?> = mutableMapOf()

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

fun <T> compose(block: ComposeCodecDsl<T>.() -> Unit): Codec<T> {
    val dsl = ComposeCodecDslImpl<T>()
    dsl.block()
    return ComposeCodec(dsl)
}
