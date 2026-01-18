package com.testernest.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TesternestCore(
    appContext: Context,
    private val prefs: SharedPreferences = appContext.getSharedPreferences(StorageKeys.PREFS_NAME, Context.MODE_PRIVATE),
    private val logger: TesternestLogger = TesternestLogger(),
    private val jsonEncoder: JsonEncoder = JsonEncoder(),
    private val httpClient: HttpClient = HttpClient(jsonEncoder, logger),
    private val queue: EventQueue = EventQueue(),
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(),
    private val appInfo: AppInfo = AppInfo.from(appContext),
    private val retryPolicy: RetryPolicy = RetryPolicy()
) {
    companion object {
        const val SDK_VERSION = "0.1.2-android"
    }

    @Volatile
    private var currentScreen: String? = null
    @Volatile
    private var initialized = false
    @Volatile
    private var schedulerStarted = false
    @Volatile
    private var lastError: String? = null

    private val bootstrapLock = Any()
    @Volatile
    private var bootstrapInProgress = false
    private var authFailureCount = 0

    private var sessionId: String = UUID.randomUUID().toString()

    fun init(baseUrl: String, publicKey: String, enableLogs: Boolean) {
        logger.enabled = enableLogs
        StorageKeys.write(prefs) {
            putString(StorageKeys.BASE_URL, baseUrl)
            putString(StorageKeys.PUBLIC_KEY, publicKey)
            putLong(StorageKeys.SAVED_AT, nowSeconds())
        }
        initialized = true
        startSchedulerIfNeeded()
        executor.execute {
            ensureBootstrapped(force = false)
            logEventInternal("app_open", null)
            flushInternal("init")
        }
    }

    fun isInitialized(): Boolean = initialized

    fun updatePublicKey(publicKey: String) {
        StorageKeys.write(prefs) { putString(StorageKeys.PUBLIC_KEY, publicKey) }
    }

    fun updateBaseUrl(baseUrl: String) {
        StorageKeys.write(prefs) { putString(StorageKeys.BASE_URL, baseUrl) }
    }

    fun setCurrentScreen(screen: String?) {
        currentScreen = screen
    }

    fun logEvent(name: String, properties: Map<String, Any?>?) {
        val event = EventPayload(
            name = name,
            ts = nowSeconds(),
            sessionId = sessionId,
            testerId = StorageKeys.read(prefs, StorageKeys.TESTER_ID),
            screen = currentScreen,
            properties = jsonEncoder.normalizeProperties(properties),
            packageName = appInfo.packageName,
            appVersion = appInfo.appVersion,
            buildNumber = appInfo.buildNumber,
            platform = "android",
            deviceModel = appInfo.deviceModel,
            osVersion = appInfo.osVersion
        )
        queue.add(event)
        if (queue.size() >= 10) {
            executor.execute { flushInternal("threshold") }
        }
    }

    fun flushNow() {
        executor.execute { flushInternal("manual") }
    }

    fun connectTester(code6: String) {
        val trimmed = code6.trim()
        if (!Regex("^\\d{6}$").matches(trimmed)) {
            lastError = "Only 6-digit code supported"
            if (logger.enabled) {
                Log.e("Testernest", "CLAIM FAILED: $lastError")
            }
            return
        }
        executor.execute { connectInternal(ClaimRequest(connectCode = trimmed)) }
    }

    fun disconnectTester() {
        executor.execute {
            StorageKeys.write(prefs) {
                remove(StorageKeys.CONNECTED_TESTER_ID)
                remove(StorageKeys.CONNECTED_PUBLIC_KEY)
                remove(StorageKeys.CONNECTED_SESSION_TOKEN)
                remove(StorageKeys.CONNECTED_REFRESH_TOKEN)
                remove(StorageKeys.CONNECTED_AT)
                remove(StorageKeys.TESTER_ID)
                remove(StorageKeys.ACCESS_TOKEN)
                remove(StorageKeys.INGEST_URL)
            }
            ensureBootstrapped(force = true)
        }
    }

    fun isTesterConnected(): Boolean {
        return !StorageKeys.read(prefs, StorageKeys.CONNECTED_TESTER_ID).isNullOrBlank()
    }

    fun getDebugSnapshot(): Map<String, Any?> {
        val testerId = StorageKeys.read(prefs, StorageKeys.TESTER_ID)
        return mapOf(
            "baseUrl" to StorageKeys.read(prefs, StorageKeys.BASE_URL),
            "publicKeyPresent" to !StorageKeys.read(prefs, StorageKeys.PUBLIC_KEY).isNullOrBlank(),
            "testerIdPrefix" to testerId?.take(6),
            "queueLength" to queue.size(),
            "lastError" to lastError
        )
    }

    fun logEventInternal(name: String, properties: Map<String, Any?>?) {
        logEvent(name, properties)
    }

    private fun startSchedulerIfNeeded() {
        if (schedulerStarted) return
        schedulerStarted = true
        executor.scheduleAtFixedRate({ flushInternal("timer") }, 12, 12, TimeUnit.SECONDS)
    }

    private fun connectInternal(request: ClaimRequest) {
        if (!ensureBootstrapped(force = false)) {
            return
        }
        val token = StorageKeys.read(prefs, StorageKeys.ACCESS_TOKEN) ?: return
        val baseUrl = StorageKeys.read(prefs, StorageKeys.BASE_URL) ?: return
        val url = UrlBuilder.resolveUrl(baseUrl, "/api/v1/mobile/claim")
        if (logger.enabled) {
            Log.i(
                "Testernest",
                "CLAIM -> url=$url"
            )
        }
        val response = httpClient.post(url, request, token)
        if (logger.enabled) {
            Log.i("Testernest", "CLAIM <- status=${response.code}")
        }
        if (response.success) {
            response.body?.let { storeAuthResponse(it, isConnected = true) }
            flushInternal("claim")
        } else {
            if (logger.enabled) {
                Log.e(
                    "Testernest",
                    "CLAIM FAILED status=${response.code} bodySnippet=${response.bodySnippet.orEmpty()}"
                )
            }
            lastError = response.error
        }
    }

    private fun flushInternal(reason: String) {
        if (queue.size() == 0) return
        if (!ensureBootstrapped(force = false)) {
            return
        }
        val accessToken = StorageKeys.read(prefs, StorageKeys.ACCESS_TOKEN) ?: return
        val baseUrl = StorageKeys.read(prefs, StorageKeys.BASE_URL) ?: return
        val ingestUrl = StorageKeys.read(prefs, StorageKeys.INGEST_URL)
            ?: "/api/v1/mobile/events/batch"
        val resolvedIngestUrl = UrlBuilder.resolveUrl(baseUrl, ingestUrl)

        val batch = queue.peek(max = 50)
        if (batch.isEmpty()) return

        var result = sendBatch(resolvedIngestUrl, batch, accessToken)
        if (isInvalidToken(result)) {
            logger.info("BATCH 401 -> clearing token -> BOOTSTRAP -> retry batch")
            onAuthInvalid(result.bodySnippet)
            if (ensureBootstrapped(force = true)) {
                val newToken = StorageKeys.read(prefs, StorageKeys.ACCESS_TOKEN)
                if (newToken.isNullOrBlank()) {
                    logger.info("BATCH retry aborted: missing new access token")
                    result = result.copy(success = false, error = "Missing access token after bootstrap")
                } else {
                    result = sendBatch(resolvedIngestUrl, batch, newToken)
                    if (result.success) {
                        logger.info("BATCH retry success")
                    } else {
                        logger.info("BATCH retry failed status=${result.code}")
                    }
                }
            } else {
                logger.info("BOOTSTRAP failed; keeping queue for retry")
            }
        } else if (!result.success) {
            result = retrySend(resolvedIngestUrl, batch, accessToken)
        }

        if (result.success) {
            queue.remove(batch.size)
        } else {
            lastError = result.error
        }
    }

    private fun retrySend(ingestUrl: String, batch: List<EventPayload>, accessToken: String): NetworkResult {
        return retryPolicy.run(
            action = { sendBatch(ingestUrl, batch, accessToken) },
            isSuccess = { it.success }
        )
    }

    private fun sendBatch(ingestUrl: String, batch: List<EventPayload>, accessToken: String): NetworkResult {
        logger.info("BATCH -> $ingestUrl count=${batch.size}")
        val result = httpClient.postEvents(ingestUrl, batch, accessToken)
        val remainingQueue = if (result.success) {
            (queue.size() - batch.size).coerceAtLeast(0)
        } else {
            queue.size()
        }
        logger.info("BATCH <- status=${result.code} remainingQueue=$remainingQueue")
        if (!result.success) {
            logger.error("BATCH FAILED status=${result.code} bodySnippet=${result.bodySnippet.orEmpty()}")
        }
        return result
    }

    private fun isInvalidToken(result: NetworkResult): Boolean {
        val snippet = result.bodySnippet ?: ""
        return result.code == 401 && snippet.contains("invalid token", ignoreCase = true)
    }

    private fun onAuthInvalid(bodySnippet: String?) {
        authFailureCount += 1
        StorageKeys.write(prefs) {
            remove(StorageKeys.ACCESS_TOKEN)
            remove(StorageKeys.INGEST_URL)
            remove(StorageKeys.CONNECTED_SESSION_TOKEN)
            remove(StorageKeys.CONNECTED_REFRESH_TOKEN)
            remove(StorageKeys.CONNECTED_AT)
        }
        logger.info("AUTH INVALID -> cleared token authFailures=$authFailureCount bodySnippet=${bodySnippet.orEmpty()}")
    }

    private fun ensureBootstrapped(force: Boolean): Boolean {
        val baseUrl = StorageKeys.read(prefs, StorageKeys.BASE_URL)
        val publicKey = StorageKeys.read(prefs, StorageKeys.PUBLIC_KEY)
        if (baseUrl.isNullOrBlank() || publicKey.isNullOrBlank()) {
            lastError = "Missing baseUrl or publicKey"
            logger.error("BOOTSTRAP skipped: missing baseUrl or publicKey")
            return false
        }
        if (!force && !StorageKeys.read(prefs, StorageKeys.ACCESS_TOKEN).isNullOrBlank()) {
            return true
        }

        synchronized(bootstrapLock) {
            if (!force && !StorageKeys.read(prefs, StorageKeys.ACCESS_TOKEN).isNullOrBlank()) {
                return true
            }
            if (bootstrapInProgress) {
                logger.info("BOOTSTRAP already in progress; skipping")
                return false
            }
            bootstrapInProgress = true
        }

        val url = UrlBuilder.resolveUrl(baseUrl, "/api/v1/mobile/bootstrap")
        val testerId = StorageKeys.read(prefs, StorageKeys.TESTER_ID)
        if (logger.enabled) {
            Log.i("Testernest", "BOOTSTRAP -> url=$url publicKeyPrefix=${publicKey.take(6)}")
        }
        val request = BootstrapRequest(
            publicKey = publicKey,
            ts = nowSeconds(),
            sdkVersion = SDK_VERSION,
            testerId = testerId,
            packageName = appInfo.packageName,
            appVersion = appInfo.appVersion,
            buildNumber = appInfo.buildNumber,
            platform = "android",
            deviceModel = appInfo.deviceModel,
            osVersion = appInfo.osVersion
        )
        val response = httpClient.post(url, request, null, "BOOTSTRAP")
        if (logger.enabled) {
            Log.i(
                "Testernest",
                "BOOTSTRAP <- status=${response.code} bodySnippet=${response.bodySnippet.orEmpty()}"
            )
        }
        return if (response.success) {
            response.body?.let { storeAuthResponse(it, isConnected = false) }
            true
        } else {
            lastError = response.error
            false
        }.also {
            synchronized(bootstrapLock) {
                bootstrapInProgress = false
            }
        }
    }

    private fun storeAuthResponse(response: AuthResponse, isConnected: Boolean) {
        val baseUrl = StorageKeys.read(prefs, StorageKeys.BASE_URL) ?: return
        val ingestUrl = UrlBuilder.resolveUrl(baseUrl, response.ingestUrl ?: "/api/v1/mobile/events/batch")
        StorageKeys.write(prefs) {
            putString(StorageKeys.TESTER_ID, response.testerId)
            putString(StorageKeys.ACCESS_TOKEN, response.accessToken)
            putString(StorageKeys.INGEST_URL, ingestUrl)
            putLong(StorageKeys.SAVED_AT, nowSeconds())
            if (isConnected) {
                putString(StorageKeys.CONNECTED_TESTER_ID, response.testerId)
                putString(StorageKeys.CONNECTED_PUBLIC_KEY, StorageKeys.read(prefs, StorageKeys.PUBLIC_KEY))
                putString(StorageKeys.CONNECTED_SESSION_TOKEN, response.sessionToken)
                putString(StorageKeys.CONNECTED_REFRESH_TOKEN, response.refreshToken)
                putLong(StorageKeys.CONNECTED_AT, nowSeconds())
            }
        }
    }

    private fun nowSeconds(): Long = System.currentTimeMillis() / 1000

    internal fun flushInternalForTest(reason: String) {
        flushInternal(reason)
    }
}
