package com.testernest.core

import org.json.JSONArray
import org.json.JSONObject

class JsonEncoder {
    fun normalizeProperties(properties: Map<String, Any?>?): Map<String, Any?>? {
        return properties?.filterKeys { it.isNotBlank() }
    }

    fun encodeBootstrap(request: BootstrapRequest): String {
        val obj = JSONObject()
        obj.put("publicKey", request.publicKey)
        obj.put("ts", request.ts)
        obj.put("sdkVersion", request.sdkVersion)
        if (!request.testerId.isNullOrBlank()) {
            obj.put("testerId", request.testerId)
        }
        obj.put("packageName", request.packageName)
        obj.put("appVersion", request.appVersion)
        obj.put("buildNumber", request.buildNumber)
        obj.put("platform", request.platform)
        obj.put("deviceModel", request.deviceModel)
        obj.put("osVersion", request.osVersion)
        return obj.toString()
    }

    fun encodeClaim(request: ClaimRequest): String {
        return JSONObject().put("connectCode", request.connectCode).toString()
    }

    fun encodeEventBatch(events: List<EventPayload>): String {
        val array = JSONArray()
        for (event in events) {
            array.put(encodeEvent(event))
        }
        return JSONObject().put("events", array).toString()
    }

    fun decodeAuthResponse(jsonString: String): AuthResponse {
        val obj = JSONObject(jsonString)
        return AuthResponse(
            testerId = obj.getString("testerId"),
            accessToken = obj.getString("accessToken"),
            ingestUrl = obj.optStringOrNull("ingestUrl"),
            expiresIn = obj.optLongOrNull("expiresIn"),
            sessionToken = obj.optStringOrNull("sessionToken"),
            refreshToken = obj.optStringOrNull("refreshToken")
        )
    }

    private fun encodeEvent(event: EventPayload): JSONObject {
        val obj = JSONObject()
        obj.put("name", event.name)
        obj.put("ts", event.ts)
        obj.put("sessionId", event.sessionId)
        if (!event.testerId.isNullOrBlank()) {
            obj.put("testerId", event.testerId)
        }
        if (!event.screen.isNullOrBlank()) {
            obj.put("screen", event.screen)
        }
        event.properties?.let { props ->
            obj.put("properties", toJsonValue(props))
        }
        obj.put("packageName", event.packageName)
        obj.put("appVersion", event.appVersion)
        obj.put("buildNumber", event.buildNumber)
        obj.put("platform", event.platform)
        obj.put("deviceModel", event.deviceModel)
        obj.put("osVersion", event.osVersion)
        return obj
    }

    private fun toJsonValue(value: Any?): Any {
        return when (value) {
            null -> JSONObject.NULL
            is JSONObject -> value
            is JSONArray -> value
            is String -> value
            is Number -> value
            is Boolean -> value
            is Map<*, *> -> {
                val obj = JSONObject()
                for ((key, nestedValue) in value) {
                    if (key is String) {
                        obj.put(key, toJsonValue(nestedValue))
                    }
                }
                obj
            }
            is Iterable<*> -> {
                val array = JSONArray()
                for (item in value) {
                    array.put(toJsonValue(item))
                }
                array
            }
            else -> value.toString()
        }
    }

    private fun JSONObject.optStringOrNull(name: String): String? {
        if (!has(name)) return null
        val value = optString(name, "")
        return if (value.isBlank()) null else value
    }

    private fun JSONObject.optLongOrNull(name: String): Long? {
        if (!has(name)) return null
        return try {
            getLong(name)
        } catch (ex: Exception) {
            null
        }
    }
}
