package com.miroshnikov.messenger

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

@Composable
fun MessengerApp(contentPadding: PaddingValues, viewModel: ChatViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val chat = state.chat
    val image = state.image

    BackHandler(enabled = image != null || chat != null) {
        if (image != null) viewModel.closeImage() else viewModel.closeChat()
    }

    val channelsListState = rememberLazyListState()
    val messagesListState = rememberSaveable(chat, saver = LazyListState.Saver) { LazyListState() }
    val padded = Modifier.fillMaxSize().padding(contentPadding)

    when {
        image != null -> ImageScreen(image, contentPadding, onClose = viewModel::closeImage)

        isLandscape -> Row(padded) {
            ChannelsScreen(
                state = state,
                listState = channelsListState,
                onChannelClick = viewModel::openChat,
                onRetry = viewModel::loadChannels,
                modifier = Modifier.weight(2f),
            )
            VerticalDivider()
            if (chat != null) {
                MessagesScreen(
                    chat = chat,
                    state = state,
                    isLandscape = true,
                    listState = messagesListState,
                    onBack = viewModel::closeChat,
                    onLoadMore = viewModel::loadMore,
                    onImageClick = viewModel::openImage,
                    modifier = Modifier.weight(3f),
                )
            } else {
                CenteredText(stringResource(R.string.select_chat), "select_chat", Modifier.weight(3f))
            }
        }

        chat != null -> MessagesScreen(
            chat = chat,
            state = state,
            isLandscape = false,
            listState = messagesListState,
            onBack = viewModel::closeChat,
            onLoadMore = viewModel::loadMore,
            onImageClick = viewModel::openImage,
            modifier = padded,
        )

        else -> ChannelsScreen(
            state = state,
            listState = channelsListState,
            onChannelClick = viewModel::openChat,
            onRetry = viewModel::loadChannels,
            modifier = padded,
        )
    }
}
