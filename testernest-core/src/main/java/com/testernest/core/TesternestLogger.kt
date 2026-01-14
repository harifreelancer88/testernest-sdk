package com.testernest.core

import android.util.Log

class TesternestLogger {
    var enabled: Boolean = false

    fun log(message: String) {
        if (enabled) {
            Log.d("TesterNest", sanitize(message))
        }
    }

    fun info(message: String) {
        if (enabled) {
            Log.i("Testernest", sanitize(message))
        }
    }

    fun error(message: String) {
        if (enabled) {
            Log.e("Testernest", sanitize(message))
        }
    }

    fun error(message: String, throwable: Throwable) {
        if (enabled) {
            Log.e("Testernest", sanitize(message), throwable)
        }
    }

    private fun sanitize(message: String): String {
        var masked = message
        masked = masked.replace(Regex("(?i)Bearer\\s+[A-Za-z0-9._-]+"), "Bearer ***")
        masked = masked.replace(Regex("(?i)access[_-]?token\\s*[=:]\\s*[A-Za-z0-9._-]+"), "accessToken=***")
        masked = masked.replace(Regex("(?i)\"access[_-]?token\"\\s*:\\s*\"[^\"]+\""), "\"accessToken\":\"***\"")
        masked = masked.replace(Regex("(?i)connect[_-]?code\\s*[=:]\\s*[A-Za-z0-9._-]+"), "connectCode=***")
        masked = masked.replace(Regex("(?i)\"connect[_-]?code\"\\s*:\\s*\"[^\"]+\""), "\"connectCode\":\"***\"")
        return masked
    }
}
