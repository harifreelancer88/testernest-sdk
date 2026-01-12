package com.testernest.core

import android.content.SharedPreferences

object StorageKeys {
    const val PREFS_NAME = "testernest_sdk"

    const val BASE_URL = "base_url"
    const val PUBLIC_KEY = "public_key"
    const val ACCESS_TOKEN = "access_token"
    const val TESTER_ID = "tester_id"
    const val INGEST_URL = "ingest_url"
    const val SAVED_AT = "saved_at"

    const val CONNECTED_TESTER_ID = "connected_tester_id"
    const val CONNECTED_PUBLIC_KEY = "connected_public_key"
    const val CONNECTED_SESSION_TOKEN = "connected_session_token"
    const val CONNECTED_REFRESH_TOKEN = "connected_refresh_token"
    const val CONNECTED_AT = "connected_at"
    const val AUTO_PROMPT_SHOWN = "auto_prompt_shown"

    fun read(prefs: SharedPreferences, key: String): String? = prefs.getString(key, null)

    fun write(prefs: SharedPreferences, block: SharedPreferences.Editor.() -> Unit) {
        val editor = prefs.edit()
        editor.block()
        editor.apply()
    }
}
