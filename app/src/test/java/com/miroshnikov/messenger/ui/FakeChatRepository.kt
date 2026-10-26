package com.miroshnikov.messenger.ui

import com.miroshnikov.messenger.data.ChatMessage
import com.miroshnikov.messenger.data.ChatRepository
import com.miroshnikov.messenger.data.MessageContent
import java.io.IOException

class FakeChatRepository : ChatRepository {
    var failChannels = false
    var failMessages = false
    val channelNames = listOf("1@channel", "2@channel")
    val messages = mutableMapOf<String, List<ChatMessage>>()
    val messageRequests = mutableListOf<Pair<String, Long>>()

    override suspend fun channels(): List<String> {
        if (failChannels) throw IOException("offline")
        return channelNames
    }

    override suspend fun messagesBefore(channel: String, beforeId: Long): List<ChatMessage> {
        messageRequests += channel to beforeId
        if (failMessages) throw IOException("offline")
        return messages[channel].orEmpty()
            .filter { it.id < beforeId }
            .take(ChatRepository.PAGE_SIZE)
    }
}

/** Text messages with the given ids, newest first (as the server returns them). */
fun textMessages(ids: IntRange): List<ChatMessage> =
    ids.reversed().map { ChatMessage(it.toLong(), "user", 0, MessageContent.Text("message $it")) }
