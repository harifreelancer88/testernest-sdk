package com.testernest.core

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

open class HttpClient(private val jsonEncoder: JsonEncoder, private val logger: TesternestLogger) {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val accessTokenRegex = Regex("\"(accessToken)\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE)
    private val refreshTokenRegex = Regex("\"(refreshToken)\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE)
    private val authorizationRegex = Regex("\"(authorization)\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE)
    private val connectCodeRegex = Regex("\"(connect[_-]?code)\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE)

    open fun post(
        url: String,
        body: Any,
        bearerToken: String?,
        operation: String? = null
    ): ResponseWrapper<AuthResponse> {
        val json = when (body) {
            is BootstrapRequest -> jsonEncoder.encodeBootstrap(body)
            is ClaimRequest -> jsonEncoder.encodeClaim(body)
            else -> "{}"
        }
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody(jsonMediaType))
            .apply {
                if (!bearerToken.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $bearerToken")
                }
            }
            .build()
        return execute(request, operation)
    }

    open fun postEvents(url: String, events: List<EventPayload>, bearerToken: String): NetworkResult {
        val batchJson = jsonEncoder.encodeEventBatch(events)
        val request = Request.Builder()
            .url(url)
            .post(batchJson.toRequestBody(jsonMediaType))
            .addHeader("Authorization", "Bearer $bearerToken")
            .build()
        val result = executeRaw(request)
        return NetworkResult(
            success = result.success,
            isAuthError = result.code == 401 || result.code == 403,
            error = result.error,
            code = result.code,
            bodySnippet = result.bodySnippet
        )
    }

    private fun execute(request: Request, operation: String?): ResponseWrapper<AuthResponse> {
        val requestUrl = request.url.toString()
        if (logger.enabled) {
            Log.i("Testernest", "HTTP -> ${request.method} $requestUrl")
        }
        return try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                val snippet = buildBodySnippet(bodyString)
                if (logger.enabled) {
                    Log.i("Testernest", "HTTP <- ${response.code} $requestUrl")
                }
                if (response.isSuccessful) {
                    if (bodyString.isBlank()) {
                        ResponseWrapper(success = false, error = "Empty response body", code = response.code, bodySnippet = snippet)
                    } else {
                        val parsed = jsonEncoder.decodeAuthResponse(bodyString)
                        ResponseWrapper(success = true, body = parsed, code = response.code, bodySnippet = snippet)
                    }
                } else {
                    ResponseWrapper(success = false, error = "HTTP ${response.code}", code = response.code, bodySnippet = snippet)
                }
            }
        } catch (ex: Exception) {
            if (operation != null) {
                if (logger.enabled) {
                    Log.e(
                        "Testernest",
                        "HTTP EXCEPTION ${ex::class.java.simpleName}: ${ex.message} url=$requestUrl",
                        ex
                    )
                }
            } else {
                logger.log("Network error: ${ex.message}")
            }
            ResponseWrapper(
                success = false,
                error = ex.message,
                code = 0,
                bodySnippet = buildBodySnippet(ex.message.orEmpty())
            )
        }
    }

    private fun executeRaw(request: Request): RawResponse {
        val requestUrl = request.url.toString()
        if (logger.enabled) {
            Log.i("Testernest", "HTTP -> ${request.method} $requestUrl")
        }
        return try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (logger.enabled) {
                    Log.i("Testernest", "HTTP <- ${response.code} $requestUrl")
                }
                RawResponse(
                    success = response.isSuccessful,
                    code = response.code,
                    error = if (response.isSuccessful) null else "HTTP ${response.code}",
                    bodySnippet = buildBodySnippet(bodyString)
                )
            }
        } catch (ex: Exception) {
            if (logger.enabled) {
                Log.e(
                    "Testernest",
                    "HTTP EXCEPTION ${ex::class.java.simpleName}: ${ex.message} url=$requestUrl",
                    ex
                )
            } else {
                logger.log("Network error: ${ex.message}")
            }
            RawResponse(
                success = false,
                code = 0,
                error = ex.message,
                bodySnippet = buildBodySnippet(ex.message.orEmpty())
            )
        }
    }

    private fun buildBodySnippet(body: String): String {
        return redactSensitiveFields(body).take(200)
    }

    private fun redactSensitiveFields(body: String): String {
        if (!body.contains("{") && !body.contains("[")) {
            return body
        }
        var redacted = body
        redacted = accessTokenRegex.replace(redacted) { "\"${it.groupValues[1]}\":\"***\"" }
        redacted = refreshTokenRegex.replace(redacted) { "\"${it.groupValues[1]}\":\"***\"" }
        redacted = authorizationRegex.replace(redacted) { "\"${it.groupValues[1]}\":\"***\"" }
        redacted = connectCodeRegex.replace(redacted) { "\"${it.groupValues[1]}\":\"***\"" }
        return redacted
    }
}

data class ResponseWrapper<T>(
    val success: Boolean,
    val body: T? = null,
    val error: String? = null,
    val code: Int = 0,
    val bodySnippet: String? = null
)

data class RawResponse(
    val success: Boolean,
    val code: Int,
    val error: String?,
    val bodySnippet: String? = null
)

data class NetworkResult(
    val success: Boolean,
    val isAuthError: Boolean,
    val error: String? = null,
    val code: Int = 0,
    val bodySnippet: String? = null
)
