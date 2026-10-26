package com.miroshnikov.messenger.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miroshnikov.messenger.MessengerApplication
import com.miroshnikov.messenger.data.ChatMessage
import com.miroshnikov.messenger.data.ChatRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ChatViewModel(
    private val savedState: SavedStateHandle,
    private val repository: ChatRepository,
) : ViewModel(), ChatActions {

    private val _uiState = MutableStateFlow(UiState(openedImage = savedState[KEY_IMAGE]))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var channelsJob: Job? = null
    private var pageJob: Job? = null

    init {
        retryChannels()
        savedState.get<String>(KEY_CHANNEL)?.let(::openChat)
    }

    override fun retryChannels() {
        channelsJob?.cancel()
        _uiState.update { it.copy(channels = ChannelsState.Loading) }
        channelsJob = viewModelScope.launch {
            val result = attempt { repository.channels() }
            _uiState.update { state ->
                state.copy(
                    channels = result.fold(
                        onSuccess = { ChannelsState.Loaded(it.toImmutableList()) },
                        onFailure = { ChannelsState.Error },
                    )
                )
            }
        }
    }

    override fun selectChannel(name: String) {
        if (_uiState.value.chat?.channel == name) return
        savedState[KEY_CHANNEL] = name
        openChat(name)
    }

    override fun closeChat() {
        pageJob?.cancel()
        savedState[KEY_CHANNEL] = null
        _uiState.update { it.copy(chat = null) }
    }

    override fun loadNextPage() {
        val chat = _uiState.value.chat ?: return
        if (chat.isLoading || chat.hasError || chat.endReached) return
        fetchPage(chat)
    }

    override fun retryChat() {
        val chat = _uiState.value.chat ?: return
        if (chat.hasError) fetchPage(chat)
    }

    override fun openImage(link: String) {
        savedState[KEY_IMAGE] = link
        _uiState.update { it.copy(openedImage = link) }
    }

    override fun closeImage() {
        savedState[KEY_IMAGE] = null
        _uiState.update { it.copy(openedImage = null) }
    }

    private fun openChat(name: String) {
        pageJob?.cancel()
        val chat = ChatState(channel = name)
        _uiState.update { it.copy(chat = chat) }
        fetchPage(chat)
    }

    private fun fetchPage(chat: ChatState) {
        val beforeId = chat.messages.lastOrNull()?.id ?: ChatRepository.LATEST_ID
        _uiState.update { it.copy(chat = chat.copy(isLoading = true, hasError = false)) }
        pageJob = viewModelScope.launch {
            val result = attempt { repository.messagesBefore(chat.channel, beforeId) }
            _uiState.update { state ->
                val current = state.chat?.takeIf { it.channel == chat.channel } ?: return@update state
                state.copy(
                    chat = result.fold(
                        onSuccess = { page -> current.appendOlder(page, beforeId) },
                        onFailure = { current.copy(isLoading = false, hasError = true) },
                    )
                )
            }
        }
    }

    companion object {
        private const val KEY_CHANNEL = "channel"
        private const val KEY_IMAGE = "image"

        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MessengerApplication
                ChatViewModel(createSavedStateHandle(), app.repository)
            }
        }
    }
}

private fun ChatState.appendOlder(page: List<ChatMessage>, beforeId: Long): ChatState {
    val older = page.filter { it.id < beforeId }
    return copy(
        messages = messages.addAll(older),
        isLoading = false,
        endReached = page.size < ChatRepository.PAGE_SIZE || older.isEmpty(),
    )
}

private inline fun <T> attempt(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
