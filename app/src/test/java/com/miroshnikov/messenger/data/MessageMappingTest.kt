package com.miroshnikov.messenger.data

import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageMappingTest {
    private fun decode(json: String): List<ChatMessage> =
        chatJson.decodeFromString(ListSerializer(MessageDto.serializer()), json).map { it.toChatMessage() }

    @Test
    fun decodesTextImageAndUnknownMessages() {
        val json = """
            [
              {"id":2,"from":"tool","to":"1@channel","data":{"Image":{"link":"tool/tmp/scala-spiral.png"}},"time":1665412956487},
              {"id":3,"from":"curl","to":"1@channel","data":{"Text":{"text":"hello there"}},"time":1665413035600,"extra":true},
              {"id":4,"from":"ghost","to":"1@channel","data":{},"time":1}
            ]
        """.trimIndent()

        assertEquals(
            listOf(
                ChatMessage(2, "tool", 1665412956487, MessageContent.Image("tool/tmp/scala-spiral.png")),
                ChatMessage(3, "curl", 1665413035600, MessageContent.Text("hello there")),
                ChatMessage(4, "ghost", 1, MessageContent.Unsupported),
            ),
            decode(json),
        )
    }

    @Test
    fun imageWithoutLinkIsUnsupported() {
        val json = """[{"id":5,"from":"x","data":{"Image":{}}}]"""

        assertEquals(listOf(ChatMessage(5, "x", 0, MessageContent.Unsupported)), decode(json))
    }
}
