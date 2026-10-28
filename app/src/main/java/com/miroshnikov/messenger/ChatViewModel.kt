package com.miroshnikov.messenger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class ChatUiState(
    val channels: ImmutableList<String> = persistentListOf(),
    val channelsLoading: Boolean = true,
    val channelsError: Boolean = false,
    val chat: String? = null,
    val messages: ImmutableList<Message> = persistentListOf(),
    val messagesLoading: Boolean = false,
    val messagesError: Boolean = false,
    val allLoaded: Boolean = false,
    val image: String? = null,
)

class ChatViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState(image = savedState["image"]))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var messagesJob: Job? = null

    init {
        loadChannels()
        savedState.get<String>("chat")?.let { openChat(it) }
    }

    fun loadChannels() {
        _state.update { it.copy(channelsLoading = true, channelsError = false) }
        viewModelScope.launch {
            try {
                val channels = ChatServer.api.channels().distinct()
                _state.update { it.copy(channels = channels.toImmutableList(), channelsLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(channelsLoading = false, channelsError = true) }
            }
        }
    }

    fun openChat(name: String) {
        if (_state.value.chat == name) return
        messagesJob?.cancel()
        savedState["chat"] = name
        _state.update {
            it.copy(
                chat = name,
                messages = persistentListOf(),
                messagesLoading = false,
                messagesError = false,
                allLoaded = false,
            )
        }
        loadMore()
    }

    fun closeChat() {
        messagesJob?.cancel()
        savedState["chat"] = null
        _state.update { it.copy(chat = null, messages = persistentListOf(), messagesLoading = false) }
    }

    fun loadMore() {
        val current = _state.value
        val chat = current.chat ?: return
        if (current.messagesLoading || current.allLoaded) return
        val lastKnownId = current.messages.lastOrNull()?.id ?: Int.MAX_VALUE.toLong()
        _state.update { it.copy(messagesLoading = true, messagesError = false) }
        messagesJob = viewModelScope.launch {
            try {
                val page = ChatServer.api.messages(chat, ChatServer.PAGE_SIZE, lastKnownId)
                _state.update {
                    it.copy(
                        messages = (it.messages + page).distinctBy { message -> message.id }.toImmutableList(),
                        messagesLoading = false,
                        allLoaded = page.size < ChatServer.PAGE_SIZE,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(messagesLoading = false, messagesError = true) }
            }
        }
    }

    fun openImage(link: String) {
        savedState["image"] = link
        _state.update { it.copy(image = link) }
    }

    fun closeImage() {
        savedState["image"] = null
        _state.update { it.copy(image = null) }
    }
}
