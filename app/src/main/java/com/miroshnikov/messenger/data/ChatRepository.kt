package com.miroshnikov.messenger.data

interface ChatRepository {
    suspend fun channels(): List<String>

    suspend fun messagesBefore(channel: String, beforeId: Long): List<ChatMessage>

    companion object {
        const val PAGE_SIZE = 20

        const val LATEST_ID = 2_147_483_647L
    }
}

class NetworkChatRepository(private val api: ChatApi) : ChatRepository {
    override suspend fun channels(): List<String> = api.channels().distinct()

    override suspend fun messagesBefore(channel: String, beforeId: Long): List<ChatMessage> =
        api.messages(
            channel = channel,
            limit = ChatRepository.PAGE_SIZE,
            lastKnownId = beforeId,
            reverse = true,
        ).map { it.toChatMessage() }
}
