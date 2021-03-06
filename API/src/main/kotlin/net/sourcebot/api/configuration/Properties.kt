package net.sourcebot.api.configuration

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer

open class Properties @JsonCreator constructor(
    private val json: ObjectNode
) {
    @JvmOverloads
    fun <T> optional(path: String, type: Class<T>, supplier: () -> T? = { null }): T? {
        val levels = path.split(".").iterator()
        var lastElem: JsonNode = json[levels.next()] ?: return supplier()
        while (levels.hasNext()) {
            lastElem = lastElem[levels.next()]
        }
        return JsonSerial.fromJson(lastElem, type) ?: supplier()
    }

    @JvmOverloads
    inline fun <reified T> optional(
        path: String,
        noinline supplier: () -> T? = { null }
    ) = optional(path, T::class.java, supplier)

    @JvmOverloads
    fun <T> required(
        path: String,
        type: Class<T>,
        supplier: () -> T? = { null }
    ) = optional(path, type, supplier) ?: throw IllegalArgumentException("Could not load value at '$path'!")

    @JvmOverloads
    inline fun <reified T> required(
        path: String,
        noinline supplier: () -> T? = { null }
    ) = required(path, T::class.java, supplier)

    class Serial : JsonSerial<Properties> {
        override val serializer = object : StdSerializer<Properties>(Properties::class.java) {
            override fun serialize(
                value: Properties,
                gen: JsonGenerator,
                provider: SerializerProvider
            ) = gen.writeTree(value.json)
        }
        override val deserializer = object : StdDeserializer<Properties>(Properties::class.java) {
            override fun deserialize(
                p: JsonParser,
                ctxt: DeserializationContext
            ): Properties = Properties(p.readValueAsTree())
        }
    }
}