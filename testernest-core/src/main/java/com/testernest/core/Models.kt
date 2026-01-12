package com.testernest.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
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

@Serializable
data class ClaimRequest(
    val connectCode: String
)

@Serializable
data class AuthResponse(
    val testerId: String,
    val accessToken: String,
    val ingestUrl: String? = null,
    val expiresIn: Long? = null,
    val sessionToken: String? = null,
    val refreshToken: String? = null
)

@Serializable
data class EventPayload(
    val name: String,
    val ts: Long,
    val sessionId: String,
    val testerId: String? = null,
    val screen: String? = null,
    val properties: JsonObject? = null,
    val packageName: String,
    val appVersion: String,
    val buildNumber: String,
    val platform: String,
    val deviceModel: String,
    val osVersion: String
)

@Serializable
data class EventBatch(
    @SerialName("events") val events: List<EventPayload>
)
