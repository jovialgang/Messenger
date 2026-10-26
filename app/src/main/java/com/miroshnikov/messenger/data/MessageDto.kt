package com.miroshnikov.messenger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: Long,
    val from: String = "",
    val to: String = "",
    val data: MessageDataDto = MessageDataDto(),
    val time: Long = 0,
)

@Serializable
data class MessageDataDto(
    @SerialName("Text") val text: TextDto? = null,
    @SerialName("Image") val image: ImageDto? = null,
)

@Serializable
data class TextDto(val text: String = "")

@Serializable
data class ImageDto(val link: String? = null)
