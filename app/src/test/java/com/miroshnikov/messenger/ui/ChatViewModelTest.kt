package com.miroshnikov.messenger.ui

import androidx.lifecycle.SavedStateHandle
import com.miroshnikov.messenger.data.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val repository = FakeChatRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) = ChatViewModel(handle, repository)

    private fun ChatViewModel.chat(): ChatState = checkNotNull(uiState.value.chat)

    @Test
    fun loadsChannelsOnStart() {
        val channels = viewModel().uiState.value.channels

        assertEquals(listOf("1@channel", "2@channel"), (channels as ChannelsState.Loaded).names)
    }

    @Test
    fun channelsErrorCanBeRetried() {
        repository.failChannels = true
        val vm = viewModel()
        assertEquals(ChannelsState.Error, vm.uiState.value.channels)

        repository.failChannels = false
        vm.retryChannels()

        assertTrue(vm.uiState.value.channels is ChannelsState.Loaded)
    }

    @Test
    fun selectingChannelLoadsLatestPage() {
        repository.messages["1@channel"] = textMessages(1..45)
        val vm = viewModel()

        vm.selectChannel("1@channel")

        val chat = vm.chat()
        assertEquals("1@channel", chat.channel)
        assertEquals((45 downTo 26).map { it.toLong() }, chat.messages.map { it.id })
        assertFalse(chat.isLoading)
        assertFalse(chat.endReached)
        assertEquals(listOf("1@channel" to ChatRepository.LATEST_ID), repository.messageRequests)
    }

    @Test
    fun nextPagesLoadOlderMessagesUntilEnd() {
        repository.messages["1@channel"] = textMessages(1..45)
        val vm = viewModel()
        vm.selectChannel("1@channel")

        vm.loadNextPage()
        vm.loadNextPage()
        vm.loadNextPage()

        val chat = vm.chat()
        assertEquals((45 downTo 1).map { it.toLong() }, chat.messages.map { it.id })
        assertTrue(chat.endReached)
        assertEquals(
            listOf("1@channel" to ChatRepository.LATEST_ID, "1@channel" to 26L, "1@channel" to 6L),
            repository.messageRequests,
        )
    }

    @Test
    fun reselectingOpenChannelDoesNotRefetch() {
        val vm = viewModel()

        vm.selectChannel("1@channel")
        vm.selectChannel("1@channel")

        assertEquals(1, repository.messageRequests.size)
    }

    @Test
    fun switchingChannelsStartsFromScratch() {
        repository.messages["1@channel"] = textMessages(1..3)
        repository.messages["2@channel"] = textMessages(10..11)
        val vm = viewModel()

        vm.selectChannel("1@channel")
        vm.selectChannel("2@channel")

        assertEquals("2@channel", vm.chat().channel)
        assertEquals(listOf(11L, 10L), vm.chat().messages.map { it.id })
    }

    @Test
    fun pageErrorBlocksPagingUntilRetry() {
        repository.failMessages = true
        val vm = viewModel()
        vm.selectChannel("1@channel")
        assertTrue(vm.chat().hasError)

        vm.loadNextPage()
        assertEquals(1, repository.messageRequests.size)

        repository.failMessages = false
        repository.messages["1@channel"] = textMessages(1..2)
        vm.retryChat()

        assertFalse(vm.chat().hasError)
        assertEquals(listOf(2L, 1L), vm.chat().messages.map { it.id })
    }

    @Test
    fun selectionAndImageSurviveRecreation() {
        repository.messages["1@channel"] = textMessages(1..3)
        val handle = SavedStateHandle()
        viewModel(handle).apply {
            selectChannel("1@channel")
            openImage("pic/1.png")
        }

        val restored = viewModel(handle).uiState.value

        assertEquals("1@channel", restored.chat?.channel)
        assertEquals(3, restored.chat?.messages?.size)
        assertEquals("pic/1.png", restored.openedImage)
    }

    @Test
    fun closedChatAndImageAreNotRestored() {
        val handle = SavedStateHandle()
        viewModel(handle).apply {
            selectChannel("1@channel")
            openImage("pic/1.png")
            closeImage()
            closeChat()
            assertNull(uiState.value.chat)
            assertNull(uiState.value.openedImage)
        }

        val restored = viewModel(handle).uiState.value

        assertNull(restored.chat)
        assertNull(restored.openedImage)
    }
}
