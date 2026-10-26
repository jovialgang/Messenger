package com.miroshnikov.messenger

import android.content.pm.ActivityInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miroshnikov.messenger.ui.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(15_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setOrientation(orientation: Int) {
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = orientation }
        composeRule.waitForIdle()
    }

    @Test
    fun opensChatKeepsItAfterRecreationAndGoesBack() {
        waitForTag(TestTags.CHANNEL_ITEM)
        composeRule.onAllNodesWithTag(TestTags.CHANNEL_ITEM)[0].performClick()
        waitForTag(TestTags.CHAT_TITLE)
        val title = composeRule.onNodeWithTag(TestTags.CHAT_TITLE)
            .fetchSemanticsNode().config[SemanticsProperties.Text].joinToString()

        composeRule.activityRule.scenario.recreate()
        waitForTag(TestTags.CHAT_TITLE)
        composeRule.onNodeWithTag(TestTags.CHAT_TITLE).assertTextEquals(title)

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitForTag(TestTags.CHANNELS_LIST)
        composeRule.onNodeWithTag(TestTags.CHAT_TITLE).assertDoesNotExist()
    }

    @Test
    fun landscapeShowsBothPanes() {
        try {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            waitForTag(TestTags.CHANNELS_LIST)
            waitForTag(TestTags.SELECT_CHAT)
        } finally {
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }
}
