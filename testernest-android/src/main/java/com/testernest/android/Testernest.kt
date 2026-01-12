package com.testernest.android

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.activity.ComponentActivity
import com.testernest.core.TesternestCore
import com.testernest.core.UrlBuilder
import com.testernest.core.StorageKeys

object Testernest : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    private const val DEFAULT_BASE_URL = "https://myappcrew-tw.pages.dev"

    private var core: TesternestCore? = null
    @Volatile
    private var autoPromptInFlight = false

    @Synchronized
    fun init(
        context: Context,
        publicKey: String,
        baseUrl: String = DEFAULT_BASE_URL,
        enableLogs: Boolean = false
    ) {
        val application = context.applicationContext as Application
        if (core == null) {
            core = TesternestCore(application)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            application.registerActivityLifecycleCallbacks(this)
        }
        core?.init(baseUrl, publicKey, enableLogs)
    }

    fun logEvent(name: String, properties: Map<String, Any?>? = null) {
        core?.logEvent(name, properties)
    }

    fun flushNow() {
        core?.flushNow()
    }

    fun setCurrentScreen(screen: String?) {
        core?.setCurrentScreen(screen)
    }

    fun connectTester(code6: String) {
        val trimmed = code6.trim()
        if (!Regex("^\\d{6}$").matches(trimmed)) {
            throw IllegalArgumentException("Only 6-digit code supported")
        }
        core?.connectTester(trimmed)
    }

    fun connectFromText(input: String, publicKeyOverride: String? = null) {
        val trimmed = input.trim()
        val codeRegex = Regex("^\\d{6}$")

        val isUrl = trimmed.startsWith("http://") || trimmed.startsWith("https://")
        val params = if (isUrl) UrlBuilder.parseQueryParams(trimmed) else emptyMap()

        val connectCode = params["connectCode"] ?: params["code"] ?: params["connect"]
        val publicKey = publicKeyOverride ?: params["publicKey"]

        if (!publicKey.isNullOrBlank()) {
            core?.updatePublicKey(publicKey)
        }

        val fallback = if (isUrl) trimmed.substringAfterLast('/') else trimmed
        when {
            !connectCode.isNullOrBlank() && codeRegex.matches(connectCode) -> connectTester(connectCode)
            codeRegex.matches(fallback) -> connectTester(fallback)
            else -> throw IllegalArgumentException("Only 6-digit code supported")
        }
    }

    fun disconnectTester() {
        core?.disconnectTester()
    }

    fun isInitialized(): Boolean = core?.isInitialized() ?: false

    fun isTesterConnected(): Boolean = core?.isTesterConnected() ?: false

    fun attachAutoConnectPrompt(
        activity: ComponentActivity,
        config: TesternestConnectPromptConfig = TesternestConnectPromptConfig()
    ) {
        if (!isInitialized()) return
        if (isTesterConnected()) return
        val prefs = activity.applicationContext.getSharedPreferences(
            StorageKeys.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        if (prefs.getBoolean(StorageKeys.AUTO_PROMPT_SHOWN, false)) return
        synchronized(this) {
            if (autoPromptInFlight) return
            autoPromptInFlight = true
        }
        activity.runOnUiThread {
            try {
                if (activity.isFinishing || activity.isDestroyed) {
                    autoPromptInFlight = false
                    return@runOnUiThread
                }
                if (isTesterConnected()) {
                    autoPromptInFlight = false
                    return@runOnUiThread
                }
                prefs.edit().putBoolean(StorageKeys.AUTO_PROMPT_SHOWN, true).apply()
                val prompt = TesternestConnectPrompt(
                    activity = activity,
                    config = config,
                    onDismiss = { autoPromptInFlight = false },
                    onConnected = { autoPromptInFlight = false }
                )
                prompt.show()
            } catch (_: Exception) {
                autoPromptInFlight = false
            }
        }
    }

    fun getDebugSnapshot(): Map<String, Any?> = core?.getDebugSnapshot() ?: emptyMap()

    override fun onStart(owner: LifecycleOwner) {
        core?.logEventInternal("app_foreground", null)
    }

    override fun onStop(owner: LifecycleOwner) {
        core?.logEventInternal("app_background", null)
        core?.flushNow()
    }

    override fun onActivityResumed(activity: Activity) {
        val screen = activity.javaClass.simpleName
        core?.setCurrentScreen(screen)
        core?.logEvent("screen_view", mapOf("screen" to screen))
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
