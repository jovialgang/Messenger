package com.miroshnikov.messenger.data

interface ChatRepository {
    suspend fun channels(): List<String>

    /** Up to [PAGE_SIZE] messages of [channel] with id less than [beforeId], newest first. */
    suspend fun messagesBefore(channel: String, beforeId: Long): List<ChatMessage>

    companion object {
        const val PAGE_SIZE = 20

        /** Greater than any real id: requesting before it returns the latest messages. */
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
