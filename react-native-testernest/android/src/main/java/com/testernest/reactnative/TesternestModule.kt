package com.testernest.reactnative

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.testernest.android.Testernest

class TesternestModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String = NAME

    @ReactMethod
    fun init(options: ReadableMap, promise: Promise) {
        try {
            val publicKey = options.getString("publicKey") ?: ""
            val baseUrl = if (options.hasKey("baseUrl")) options.getString("baseUrl") else null
            val enableLogs = if (options.hasKey("enableLogs")) options.getBoolean("enableLogs") else false

            if (publicKey.isBlank()) {
                promise.reject("E_INVALID_PUBLIC_KEY", "publicKey is required")
                return
            }

            val context = reactApplicationContext
            Log.i("TesternestRN", "init called on thread=" + Thread.currentThread().name)
            Handler(Looper.getMainLooper()).post {
                try {
                    Log.i("TesternestRN", "init running on UI thread=" + Thread.currentThread().name)
                    if (baseUrl.isNullOrBlank()) {
                        Testernest.init(context, publicKey, enableLogs = enableLogs)
                    } else {
                        Testernest.init(context, publicKey, baseUrl, enableLogs)
                    }
                    promise.resolve(null)
                } catch (e: Exception) {
                    promise.reject("E_INIT_FAILED", e.message, e)
                }
            }
        } catch (e: Exception) {
            promise.reject("E_INIT_FAILED", e.message, e)
        }
    }

    @ReactMethod
    fun track(name: String, properties: ReadableMap?) {
        val map = properties?.toHashMap()
        Testernest.logEvent(name, map)
    }

    @ReactMethod
    fun flush(promise: Promise) {
        try {
            Testernest.flushNow()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("E_FLUSH_FAILED", e.message, e)
        }
    }

    @ReactMethod
    fun setCurrentScreen(screen: String?) {
        Testernest.setCurrentScreen(screen)
    }

    @ReactMethod
    fun connectTester(code6: String, promise: Promise) {
        val trimmed = code6.trim()
        if (!Regex("^\\d{6}$").matches(trimmed)) {
            promise.reject("E_INVALID_CODE", "Only 6-digit connect code supported")
            return
        }
        try {
            Testernest.connectTester(trimmed)
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("E_CONNECT_FAILED", e.message, e)
        }
    }

    @ReactMethod
    fun disconnectTester(promise: Promise) {
        try {
            Testernest.disconnectTester()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("E_DISCONNECT_FAILED", e.message, e)
        }
    }

    @ReactMethod
    fun getDebugSnapshot(promise: Promise) {
        try {
            val snapshot = Testernest.getDebugSnapshot()
            promise.resolve(toWritableMap(snapshot))
        } catch (e: Exception) {
            promise.reject("E_DEBUG_SNAPSHOT_FAILED", e.message, e)
        }
    }

    @ReactMethod
    fun isConnected(promise: Promise) {
        try {
            promise.resolve(Testernest.isTesterConnected())
        } catch (e: Exception) {
            promise.reject("E_IS_CONNECTED_FAILED", e.message, e)
        }
    }

    private fun toWritableMap(map: Map<*, *>): WritableMap {
        val writable = Arguments.createMap()
        for ((keyAny, value) in map) {
            val key = keyAny?.toString() ?: continue
            when (value) {
                null -> writable.putNull(key)
                is Boolean -> writable.putBoolean(key, value)
                is Int -> writable.putInt(key, value)
                is Double -> writable.putDouble(key, value)
                is Float -> writable.putDouble(key, value.toDouble())
                is Long -> writable.putDouble(key, value.toDouble())
                is String -> writable.putString(key, value)
                is Map<*, *> -> writable.putMap(key, toWritableMap(value))
                is List<*> -> writable.putArray(key, toWritableArray(value))
                else -> writable.putString(key, value.toString())
            }
        }
        return writable
    }

    private fun toWritableArray(list: List<*>): WritableArray {
        val array = Arguments.createArray()
        for (value in list) {
            when (value) {
                null -> array.pushNull()
                is Boolean -> array.pushBoolean(value)
                is Int -> array.pushInt(value)
                is Double -> array.pushDouble(value)
                is Float -> array.pushDouble(value.toDouble())
                is Long -> array.pushDouble(value.toDouble())
                is String -> array.pushString(value)
                is Map<*, *> -> array.pushMap(toWritableMap(value))
                is List<*> -> array.pushArray(toWritableArray(value))
                else -> array.pushString(value.toString())
            }
        }
        return array
    }

    companion object {
        const val NAME = "Testernest"
    }
}
