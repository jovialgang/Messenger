package com.miroshnikov.messenger

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ChatApi {
    @GET("channels")
    suspend fun channels(): List<String>

    @GET("channel/{channel}")
    suspend fun messages(
        @Path("channel") channel: String,
        @Query("limit") limit: Int,
        @Query("lastKnownId") lastKnownId: Long,
        @Query("reverse") reverse: Boolean = true,
    ): List<Message>
}

object ChatServer {
    private const val BASE_URL = "https://faerytea.name/"
    const val PAGE_SIZE = 20

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val api: ChatApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ChatApi::class.java)

    fun thumbnailUrl(link: String) = BASE_URL + "thumb/" + link

    fun imageUrl(link: String) = BASE_URL + "img/" + link
}
