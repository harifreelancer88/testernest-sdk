package com.testernest.core

import java.net.URI

object UrlBuilder {
    fun combine(baseUrl: String, path: String): String {
        val normalizedBase = baseUrl.trimEnd('/')
        val normalizedPath = if (path.startsWith('/')) path else "/$path"
        return normalizedBase + normalizedPath
    }

    fun resolveUrl(baseUrl: String, maybePathOrUrl: String): String {
        return when {
            maybePathOrUrl.startsWith("http://") || maybePathOrUrl.startsWith("https://") -> maybePathOrUrl
            maybePathOrUrl.startsWith("/") -> baseUrl.trimEnd('/') + maybePathOrUrl
            else -> baseUrl.trimEnd('/') + "/" + maybePathOrUrl
        }
    }

    fun parseQueryParams(url: String): Map<String, String> {
        return try {
            val uri = URI(url)
            val query = uri.rawQuery ?: return emptyMap()
            query.split('&')
                .mapNotNull { part ->
                    val pieces = part.split('=')
                    if (pieces.isEmpty()) null else {
                        val key = pieces[0]
                        val value = if (pieces.size > 1) pieces[1] else ""
                        key to value
                    }
                }
                .toMap()
        } catch (ex: Exception) {
            emptyMap()
        }
    }
}
