package com.miroshnikov.messenger.data

import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkChatRepositoryTest {
    private class CannedResponse(private val body: String) : Interceptor {
        val requests = mutableListOf<HttpUrl>()

        override fun intercept(chain: Interceptor.Chain): Response {
            requests += chain.request().url
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private fun repository(server: CannedResponse): ChatRepository =
        NetworkChatRepository(createChatApi(OkHttpClient.Builder().addInterceptor(server).build()))

    @Test
    fun requestsLatestPageWithEncodedChannelName() = runTest {
        val server = CannedResponse("""[{"id":7,"from":"a","data":{"Text":{"text":"hi"}},"time":1}]""")

        val messages = repository(server).messagesBefore("t r o@channel", ChatRepository.LATEST_ID)

        assertEquals(listOf(ChatMessage(7, "a", 1, MessageContent.Text("hi"))), messages)
        val url = server.requests.single()
        assertEquals("/channel/t%20r%20o@channel", url.encodedPath)
        assertEquals("20", url.queryParameter("limit"))
        assertEquals("2147483647", url.queryParameter("lastKnownId"))
        assertEquals("true", url.queryParameter("reverse"))
    }

    @Test
    fun channelsAreDeduplicated() = runTest {
        val server = CannedResponse("""["a@channel","b@channel","a@channel"]""")

        assertEquals(listOf("a@channel", "b@channel"), repository(server).channels())
        assertEquals("/channels", server.requests.single().encodedPath)
    }
}
