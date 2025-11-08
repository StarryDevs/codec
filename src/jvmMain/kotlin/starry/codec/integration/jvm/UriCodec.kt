package starry.codec.integration.jvm

import starry.codec.Codec
import starry.codec.InputSource
import starry.codec.OutputTarget
import starry.codec.StringCodec
import java.net.URI
import java.net.URL

object UriCodec : Codec<URI> {

    override fun decode(input: InputSource): URI = URI(StringCodec.decode(input))
    override fun encode(output: OutputTarget, value: URI) = StringCodec.encode(output, value.toString())

}

object UrlCodec : Codec<URL> {

    override fun decode(input: InputSource): URL = UriCodec.decode(input).toURL()
    override fun encode(output: OutputTarget, value: URL) = UriCodec.encode(output, value.toURI())

}
