package com.miroshnikov.messenger.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miroshnikov.messenger.R

@Composable
fun MessengerScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    MessengerContent(state, isLandscape, viewModel, contentPadding, modifier)
}

@Composable
fun MessengerContent(
    state: UiState,
    isLandscape: Boolean,
    actions: ChatActions,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val chat = state.chat
    val image = state.openedImage

    BackHandler(enabled = image != null || chat != null) {
        if (image != null) actions.closeImage() else actions.closeChat()
    }

    // Hoisted here so scroll positions survive rotation and an opened image.
    val channelsListState = rememberLazyListState()
    val messagesListState = rememberSaveable(chat?.channel, saver = LazyListState.Saver) { LazyListState() }

    when {
        image != null -> ImageScreen(image, contentPadding, actions::closeImage, modifier)

        isLandscape -> Row(
            modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            ChannelsPane(
                state = state.channels,
                selectedChannel = chat?.channel,
                listState = channelsListState,
                onSelect = actions::selectChannel,
                onRetry = actions::retryChannels,
                modifier = Modifier.weight(2f),
            )
            VerticalDivider()
            if (chat != null) {
                MessagesPane(
                    chat = chat,
                    isLandscape = true,
                    listState = messagesListState,
                    onBack = actions::closeChat,
                    onLoadMore = actions::loadNextPage,
                    onRetry = actions::retryChat,
                    onOpenImage = actions::openImage,
                    modifier = Modifier.weight(3f),
                )
            } else {
                CenteredText(stringResource(R.string.select_chat), TestTags.SELECT_CHAT, Modifier.weight(3f))
            }
        }

        chat != null -> MessagesPane(
            chat = chat,
            isLandscape = false,
            listState = messagesListState,
            onBack = actions::closeChat,
            onLoadMore = actions::loadNextPage,
            onRetry = actions::retryChat,
            onOpenImage = actions::openImage,
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
        )

        else -> ChannelsPane(
            state = state.channels,
            selectedChannel = null,
            listState = channelsListState,
            onSelect = actions::selectChannel,
            onRetry = actions::retryChannels,
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
    }
}
