package com.miroshnikov.messenger.ui

import com.miroshnikov.messenger.data.ChatMessage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class UiState(
    val channels: ChannelsState = ChannelsState.Loading,
    val chat: ChatState? = null,
    val openedImage: String? = null,
)

sealed interface ChannelsState {
    data object Loading : ChannelsState
    data object Error : ChannelsState
    data class Loaded(val names: ImmutableList<String>) : ChannelsState
}

data class ChatState(
    val channel: String,
    /** Newest first. */
    val messages: PersistentList<ChatMessage> = persistentListOf(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val endReached: Boolean = false,
)
