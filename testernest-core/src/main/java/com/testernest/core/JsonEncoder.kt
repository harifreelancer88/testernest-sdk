package com.testernest.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class JsonEncoder {
    private val json = Json {
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun toJsonObject(properties: Map<String, Any?>?): JsonObject? {
        if (properties == null) return null
        val map = properties.mapValues { toJsonElement(it.value) }
        return JsonObject(map)
    }

    private fun toJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is JsonElement -> value
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> {
                val nested = value.entries
                    .filter { it.key is String }
                    .associate { it.key as String to toJsonElement(it.value) }
                JsonObject(nested)
            }
            is Iterable<*> -> JsonArray(value.map { toJsonElement(it) })
            else -> JsonPrimitive(value.toString())
        }
    }

    fun <T> decode(jsonString: String, deserializer: kotlinx.serialization.DeserializationStrategy<T>): T {
        return json.decodeFromString(deserializer, jsonString)
    }

    fun <T> encodeToString(serializer: kotlinx.serialization.SerializationStrategy<T>, value: T): String {
        return json.encodeToString(serializer, value)
    }
}
