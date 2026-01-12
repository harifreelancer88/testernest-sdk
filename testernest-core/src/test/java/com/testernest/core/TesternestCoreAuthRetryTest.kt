package com.testernest.core

import android.content.ContextWrapper
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TesternestCoreAuthRetryTest {
    @Test
    fun batch401TriggersBootstrapAndRetry() {
        val prefs = InMemoryPreferences()
        StorageKeys.write(prefs) {
            putString(StorageKeys.BASE_URL, "https://example.com")
            putString(StorageKeys.PUBLIC_KEY, "public_key")
            putString(StorageKeys.ACCESS_TOKEN, "bad_token")
            putString(StorageKeys.TESTER_ID, "tester_old")
        }

        val logger = TesternestLogger()
        val jsonEncoder = JsonEncoder()
        val fakeHttp = FakeHttpClient(jsonEncoder, logger)
        val appInfo = AppInfo(
            packageName = "com.testernest.example",
            appVersion = "1.0.0",
            buildNumber = "1",
            deviceModel = "device",
            osVersion = "Android 1"
        )

        val core = TesternestCore(
            ContextWrapper(null),
            prefs = prefs,
            logger = logger,
            jsonEncoder = jsonEncoder,
            httpClient = fakeHttp,
            queue = EventQueue(),
            executor = ImmediateScheduledExecutorService(),
            appInfo = appInfo,
            retryPolicy = RetryPolicy()
        )

        core.logEvent("test_event", null)
        core.flushInternalForTest("test")

        assertEquals(1, fakeHttp.bootstrapCalls)
        assertEquals(2, fakeHttp.postEventsCalls)
        assertEquals("new_token", StorageKeys.read(prefs, StorageKeys.ACCESS_TOKEN))
        val snapshot = core.getDebugSnapshot()
        assertEquals(0, snapshot["queueLength"])
        assertNotNull(StorageKeys.read(prefs, StorageKeys.TESTER_ID))
    }
}

private class FakeHttpClient(
    jsonEncoder: JsonEncoder,
    logger: TesternestLogger
) : HttpClient(jsonEncoder, logger) {
    var postEventsCalls = 0
    var bootstrapCalls = 0

    override fun post(
        url: String,
        body: Any,
        bearerToken: String?,
        operation: String?
    ): ResponseWrapper<AuthResponse> {
        bootstrapCalls += 1
        return ResponseWrapper(
            success = true,
            body = AuthResponse(
                testerId = "tester_new",
                accessToken = "new_token",
                ingestUrl = "/api/v1/mobile/events/batch"
            ),
            code = 200,
            bodySnippet = "{\"accessToken\":\"new_token\"}"
        )
    }

    override fun postEvents(url: String, events: List<EventPayload>, bearerToken: String): NetworkResult {
        postEventsCalls += 1
        return if (postEventsCalls == 1) {
            NetworkResult(
                success = false,
                isAuthError = true,
                error = "HTTP 401",
                code = 401,
                bodySnippet = "{\"error\":\"Invalid token\"}"
            )
        } else {
            NetworkResult(
                success = true,
                isAuthError = false,
                code = 200,
                bodySnippet = "{\"ok\":true}"
            )
        }
    }
}

private class InMemoryPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = data.toMutableMap()

    override fun getString(key: String, defValue: String?): String? =
        data[key] as? String ?: defValue

    override fun getLong(key: String, defValue: Long): Long =
        data[key] as? Long ?: defValue

    override fun contains(key: String): Boolean = data.containsKey(key)

    override fun edit(): SharedPreferences.Editor = Editor(data)

    override fun getInt(key: String, defValue: Int): Int = data[key] as? Int ?: defValue

    override fun getFloat(key: String, defValue: Float): Float = data[key] as? Float ?: defValue

    override fun getBoolean(key: String, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return data[key] as? MutableSet<String> ?: defValues
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = Unit

    private class Editor(private val data: MutableMap<String, Any?>) : SharedPreferences.Editor {
        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            data[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            data[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            data.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            data.clear()
            return this
        }

        override fun apply() = Unit

        override fun commit(): Boolean = true

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            data[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            data[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            data[key] = value
            return this
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            data[key] = values
            return this
        }
    }
}

private class ImmediateScheduledExecutorService : ScheduledExecutorService {
    override fun execute(command: Runnable) {
        command.run()
    }

    override fun shutdown() = Unit

    override fun shutdownNow(): MutableList<Runnable> = mutableListOf()

    override fun isShutdown(): Boolean = false

    override fun isTerminated(): Boolean = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = true

    override fun <T> submit(task: Callable<T>): java.util.concurrent.Future<T> =
        CompletedScheduledFuture(task.call())

    override fun <T> submit(task: Runnable, result: T): java.util.concurrent.Future<T> {
        task.run()
        return CompletedScheduledFuture(result)
    }

    override fun submit(task: Runnable): java.util.concurrent.Future<*> {
        task.run()
        return CompletedScheduledFuture(null)
    }

    override fun <T> invokeAll(tasks: MutableCollection<out Callable<T>>): MutableList<java.util.concurrent.Future<T>> {
        return tasks.map { CompletedScheduledFuture(it.call()) }.toMutableList()
    }

    override fun <T> invokeAll(
        tasks: MutableCollection<out Callable<T>>,
        timeout: Long,
        unit: TimeUnit
    ): MutableList<java.util.concurrent.Future<T>> = invokeAll(tasks)

    override fun <T> invokeAny(tasks: MutableCollection<out Callable<T>>): T = tasks.first().call()

    override fun <T> invokeAny(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): T =
        tasks.first().call()

    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
        command.run()
        return CompletedScheduledFuture(null)
    }

    override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V> =
        CompletedScheduledFuture(callable.call())

    override fun scheduleAtFixedRate(
        command: Runnable,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        command.run()
        return CompletedScheduledFuture(null)
    }

    override fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit
    ): ScheduledFuture<*> {
        command.run()
        return CompletedScheduledFuture(null)
    }
}

private class CompletedScheduledFuture<T>(private val value: T?) : ScheduledFuture<T> {
    @Suppress("UNCHECKED_CAST")
    override fun get(): T = value as T

    @Suppress("UNCHECKED_CAST")
    override fun get(timeout: Long, unit: TimeUnit): T = value as T

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false

    override fun isCancelled(): Boolean = false

    override fun isDone(): Boolean = true

    override fun getDelay(unit: TimeUnit): Long = 0L

    override fun compareTo(other: java.util.concurrent.Delayed): Int = 0
}
