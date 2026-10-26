package com.miroshnikov.messenger.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerUrlsTest {
    @Test
    fun thumbnailUrl() {
        assertEquals(
            "https://faerytea.name/thumb/tool/tmp/scala-spiral.png",
            ServerUrls.thumbnail("tool/tmp/scala-spiral.png"),
        )
    }

    @Test
    fun imageUrl() {
        assertEquals(
            "https://faerytea.name/img/tool/tmp/scala-spiral.png",
            ServerUrls.image("tool/tmp/scala-spiral.png"),
        )
    }

    @Test
    fun encodesSpacesAndCyrillic() {
        assertEquals(
            "https://faerytea.name/img/my%20dir/%D0%BA%D0%BE%D1%82.png",
            ServerUrls.image("my dir/кот.png"),
        )
    }

    @Test
    fun ignoresLeadingSlash() {
        assertEquals("https://faerytea.name/img/a/b.png", ServerUrls.image("/a/b.png"))
    }

    @Test
    fun clientWaitsForSlowServer() {
        val client = createOkHttpClient()

        assertEquals(30_000, client.connectTimeoutMillis)
        assertEquals(30_000, client.readTimeoutMillis)
    }
}
