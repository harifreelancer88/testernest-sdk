package com.testernest.unity

import android.content.Context
import android.util.Log
import com.testernest.android.Testernest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class TesternestUnityBridge {
    companion object {
        private const val TAG = "TesternestUnity"
        private const val DEFAULT_BASE_URL = "https://myappcrew-tw.pages.dev"
        private var logsEnabled: Boolean = false

        @JvmStatic
        fun init(context: Context, publicKey: String, baseUrl: String?, enableLogs: Boolean) {
            logsEnabled = enableLogs
            val resolvedBaseUrl = if (baseUrl.isNullOrBlank()) DEFAULT_BASE_URL else baseUrl
            Testernest.init(context, publicKey, resolvedBaseUrl, enableLogs)
        }

        @JvmStatic
        fun track(name: String, jsonProps: String?) {
            val props = if (jsonProps.isNullOrBlank()) {
                null
            } else {
                try {
                    jsonToMap(jsonProps)
                } catch (ex: JSONException) {
                    if (logsEnabled) {
                        Log.w(TAG, "Failed to parse jsonProps. Sending empty properties.")
                    }
                    emptyMap()
                }
            }
            Testernest.logEvent(name, props)
        }

        @JvmStatic
        fun flush() {
            Testernest.flushNow()
        }

        @JvmStatic
        fun setScreen(screen: String?) {
            Testernest.setCurrentScreen(screen)
        }

        @JvmStatic
        fun connectTester(code6: String) {
            val trimmed = code6.trim()
            if (!Regex("^\\d{6}$").matches(trimmed)) {
                throw IllegalArgumentException("Only 6-digit code supported")
            }
            Testernest.connectTester(trimmed)
        }

        @JvmStatic
        fun disconnectTester() {
            Testernest.disconnectTester()
        }

        @JvmStatic
        fun getDebugSnapshot(): String {
            val snapshot = Testernest.getDebugSnapshot()
            return JSONObject(mapToJsonCompatible(snapshot)).toString()
        }

        private fun jsonToMap(json: String): Map<String, Any?> {
            val jsonObject = JSONObject(json)
            return jsonObjectToMap(jsonObject)
        }

        private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any?> {
            val map = mutableMapOf<String, Any?>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonValueToAny(jsonObject.get(key))
            }
            return map
        }

        private fun jsonArrayToList(jsonArray: JSONArray): List<Any?> {
            val list = ArrayList<Any?>(jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                list.add(jsonValueToAny(jsonArray.get(i)))
            }
            return list
        }

        private fun jsonValueToAny(value: Any?): Any? = when (value) {
            JSONObject.NULL -> null
            is JSONObject -> jsonObjectToMap(value)
            is JSONArray -> jsonArrayToList(value)
            else -> value
        }

        private fun mapToJsonCompatible(map: Map<String, Any?>): Map<String, Any?> {
            val converted = mutableMapOf<String, Any?>()
            for ((key, value) in map) {
                converted[key] = toJsonCompatibleValue(value)
            }
            return converted
        }

        private fun toJsonCompatibleValue(value: Any?): Any? = when (value) {
            null -> null
            is Map<*, *> -> {
                val nested = mutableMapOf<String, Any?>()
                for ((k, v) in value) {
                    if (k is String) {
                        nested[k] = toJsonCompatibleValue(v)
                    }
                }
                nested
            }
            is Iterable<*> -> value.map { toJsonCompatibleValue(it) }
            is Array<*> -> value.map { toJsonCompatibleValue(it) }
            else -> value
        }
    }
}
