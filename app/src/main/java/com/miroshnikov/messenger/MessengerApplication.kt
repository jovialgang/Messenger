package com.miroshnikov.messenger

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.miroshnikov.messenger.data.ChatRepository
import com.miroshnikov.messenger.data.NetworkChatRepository
import com.miroshnikov.messenger.data.createChatApi
import okhttp3.OkHttpClient

class MessengerApplication : Application(), SingletonImageLoader.Factory {
    private val okHttpClient: OkHttpClient by lazy { OkHttpClient() }

    val repository: ChatRepository by lazy { NetworkChatRepository(createChatApi(okHttpClient)) }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient })) }
            .build()
}
