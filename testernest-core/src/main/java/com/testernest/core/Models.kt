package com.testernest.core

data class BootstrapRequest(
    val publicKey: String,
    val ts: Long,
    val sdkVersion: String,
    val testerId: String? = null,
    val packageName: String,
    val appVersion: String,
    val buildNumber: String,
    val platform: String,
    val deviceModel: String,
    val osVersion: String
)

data class ClaimRequest(
    val connectCode: String
)

data class AuthResponse(
    val testerId: String,
    val accessToken: String,
    val ingestUrl: String? = null,
    val expiresIn: Long? = null,
    val sessionToken: String? = null,
    val refreshToken: String? = null
)

data class EventPayload(
    val name: String,
    val ts: Long,
    val sessionId: String,
    val testerId: String? = null,
    val screen: String? = null,
    val properties: Map<String, Any?>? = null,
    val packageName: String,
    val appVersion: String,
    val buildNumber: String,
    val platform: String,
    val deviceModel: String,
    val osVersion: String
)

data class EventBatch(
    val events: List<EventPayload>
)
