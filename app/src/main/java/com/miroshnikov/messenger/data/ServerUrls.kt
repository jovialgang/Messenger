package com.miroshnikov.messenger.data

import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object ServerUrls {
    val base: HttpUrl = "https://faerytea.name/".toHttpUrl()

    fun thumbnail(link: String): String = file("thumb", link)

    fun image(link: String): String = file("img", link)

    private fun file(kind: String, link: String): String =
        base.newBuilder()
            .addPathSegment(kind)
            .addPathSegments(link.trimStart('/'))
            .build()
            .toString()
}

val chatJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
