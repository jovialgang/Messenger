package com.miroshnikov.messenger

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miroshnikov.messenger.data.ChatMessage
import com.miroshnikov.messenger.data.MessageContent
import com.miroshnikov.messenger.ui.ChannelsState
import com.miroshnikov.messenger.ui.ChatActions
import com.miroshnikov.messenger.ui.ChatState
import com.miroshnikov.messenger.ui.MessengerContent
import com.miroshnikov.messenger.ui.TestTags
import com.miroshnikov.messenger.ui.UiState
import com.miroshnikov.messenger.ui.theme.MessengerTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessengerContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class RecordingActions : ChatActions {
        val calls = mutableListOf<String>()
        override fun retryChannels() { calls += "retryChannels" }
        override fun selectChannel(name: String) { calls += "select:$name" }
        override fun closeChat() { calls += "closeChat" }
        override fun loadNextPage() { calls += "loadNextPage" }
        override fun retryChat() { calls += "retryChat" }
        override fun openImage(link: String) { calls += "open:$link" }
        override fun closeImage() { calls += "closeImage" }
    }

    private val actions = RecordingActions()
    private val channels = ChannelsState.Loaded(persistentListOf("1@channel", "2@channel"))
    private val chat = ChatState(
        channel = "2@channel",
        messages = persistentListOf(
            ChatMessage(2, "alice", 0, MessageContent.Image("pic/cat.png")),
            ChatMessage(1, "bob", 0, MessageContent.Text("hello")),
        ),
        endReached = true,
    )

    private fun show(state: UiState, landscape: Boolean) {
        composeRule.setContent {
            MessengerTheme {
                MessengerContent(state, landscape, actions, PaddingValues())
            }
        }
    }

    private fun pressBack() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    @Test
    fun portraitShowsChannelsAndOpensChat() {
        show(UiState(channels = channels), landscape = false)

        composeRule.onAllNodesWithTag(TestTags.CHANNEL_ITEM).assertCountEquals(2)
        composeRule.onAllNodesWithTag(TestTags.CHANNEL_ITEM)[1].performClick()

        assertEquals(listOf("select:2@channel"), actions.calls)
    }

    @Test
    fun portraitChatReplacesChannels() {
        show(UiState(channels = channels, chat = chat), landscape = false)

        composeRule.onNodeWithTag(TestTags.CHANNELS_LIST).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.CHAT_TITLE).assertTextEquals("2@channel")
        composeRule.onNodeWithTag(TestTags.MESSAGE_TEXT).assertTextEquals("hello")
        composeRule.onNodeWithTag(TestTags.BACK_BUTTON).performClick()

        assertEquals(listOf("closeChat"), actions.calls)
    }

    @Test
    fun landscapeShowsHintWithoutChat() {
        show(UiState(channels = channels), landscape = true)

        composeRule.onNodeWithTag(TestTags.CHANNELS_LIST).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SELECT_CHAT).assertIsDisplayed()
    }

    @Test
    fun landscapeMarksOpenedChat() {
        show(UiState(channels = channels, chat = chat), landscape = true)

        composeRule.onAllNodesWithTag(TestTags.CHANNEL_ITEM)[0].assertIsNotSelected()
        composeRule.onAllNodesWithTag(TestTags.CHANNEL_ITEM)[1].assertIsSelected()
        composeRule.onNodeWithTag(TestTags.MESSAGES_LIST).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SELECT_CHAT).assertDoesNotExist()
    }

    @Test
    fun clickOnThumbnailOpensImage() {
        show(UiState(channels = channels, chat = chat), landscape = false)

        composeRule.onNodeWithTag(TestTags.MESSAGE_IMAGE).performClick()

        assertEquals(listOf("open:pic/cat.png"), actions.calls)
    }

    @Test
    fun imageScreenClosesByButtonAndBack() {
        show(UiState(channels = channels, chat = chat, openedImage = "pic/cat.png"), landscape = false)

        composeRule.onNodeWithTag(TestTags.IMAGE_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.MESSAGES_LIST).assertDoesNotExist()
        composeRule.onNodeWithTag(TestTags.CLOSE_IMAGE).performClick()
        pressBack()

        assertEquals(listOf("closeImage", "closeImage"), actions.calls)
    }

    @Test
    fun backClosesChatInLandscape() {
        show(UiState(channels = channels, chat = chat), landscape = true)

        pressBack()

        assertEquals(listOf("closeChat"), actions.calls)
    }

    @Test
    fun backIsNotInterceptedOnChannels() {
        show(UiState(channels = channels), landscape = false)

        composeRule.runOnIdle {
            assertFalse(composeRule.activity.onBackPressedDispatcher.hasEnabledCallbacks())
        }
    }
}
