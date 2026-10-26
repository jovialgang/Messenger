package com.miroshnikov.messenger.data

data class ChatMessage(
    val id: Long,
    val author: String,
    val time: Long,
    val content: MessageContent,
)

sealed interface MessageContent {
    data class Text(val text: String) : MessageContent
    data class Image(val link: String) : MessageContent
    data object Unsupported : MessageContent
}

fun MessageDto.toChatMessage(): ChatMessage {
    val text = data.text
    val link = data.image?.link
    val content = when {
        text != null -> MessageContent.Text(text.text)
        link != null -> MessageContent.Image(link)
        else -> MessageContent.Unsupported
    }
    return ChatMessage(id = id, author = from, time = time, content = content)
}
