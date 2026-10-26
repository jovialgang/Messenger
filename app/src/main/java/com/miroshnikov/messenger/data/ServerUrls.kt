package com.miroshnikov.messenger.data

import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

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

/** The server is sometimes slow to respond; the default 10 s timeouts are not enough. */
fun createOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

fun createChatApi(client: OkHttpClient, baseUrl: HttpUrl = ServerUrls.base): ChatApi =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(chatJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ChatApi::class.java)
