package com.miroshnikov.messenger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.miroshnikov.messenger.R
import com.miroshnikov.messenger.data.ChatMessage
import com.miroshnikov.messenger.data.MessageContent
import com.miroshnikov.messenger.data.ServerUrls
import java.text.DateFormat
import java.util.Date

private const val LOAD_MORE_THRESHOLD = 5

@Composable
fun MessagesPane(
    chat: ChatState,
    isLandscape: Boolean,
    listState: LazyListState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpenImage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        PaneHeader {
            IconButton(onClick = onBack, modifier = Modifier.testTag(TestTags.BACK_BUTTON)) {
                if (isLandscape) {
                    Icon(painterResource(R.drawable.ic_close), stringResource(R.string.close_chat))
                } else {
                    Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.back_to_channels))
                }
            }
            HeaderTitle(chat.channel, TestTags.CHAT_TITLE, Modifier.weight(1f))
        }
        when {
            chat.messages.isNotEmpty() -> MessagesList(chat, listState, onLoadMore, onRetry, onOpenImage)
            chat.hasError -> ErrorMessage(onRetry)
            chat.endReached -> CenteredText(stringResource(R.string.no_messages), TestTags.EMPTY)
            else -> LoadingIndicator()
        }
    }
}

@Composable
private fun MessagesList(
    chat: ChatState,
    listState: LazyListState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpenImage: (String) -> Unit,
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val nearOldest by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    val canLoadMore = !chat.isLoading && !chat.hasError && !chat.endReached
    LaunchedEffect(nearOldest, canLoadMore) {
        if (nearOldest && canLoadMore) currentOnLoadMore()
    }

    LazyColumn(
        state = listState,
        reverseLayout = true,
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.MESSAGES_LIST),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(chat.messages, key = { it.id }) { message ->
            MessageItem(message, onOpenImage)
        }
        when {
            chat.isLoading -> item(key = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(Modifier.testTag(TestTags.LOADING))
                }
            }

            chat.hasError -> item(key = "error") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.ERROR),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.load_error))
                    TextButton(onClick = onRetry, modifier = Modifier.testTag(TestTags.RETRY)) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageItem(message: ChatMessage, onOpenImage: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.MESSAGE_ITEM),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.author,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .testTag(TestTags.MESSAGE_AUTHOR),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatTime(message.time),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            when (val content = message.content) {
                is MessageContent.Text -> Text(
                    text = content.text,
                    modifier = Modifier.testTag(TestTags.MESSAGE_TEXT),
                    style = MaterialTheme.typography.bodyLarge,
                )

                is MessageContent.Image -> Thumbnail(content.link, onClick = { onOpenImage(content.link) })

                MessageContent.Unsupported -> Text(
                    text = stringResource(R.string.unsupported_message),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
private fun Thumbnail(link: String, onClick: () -> Unit) {
    var failed by remember(link) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .testTag(TestTags.MESSAGE_IMAGE),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ServerUrls.thumbnail(link),
            contentDescription = stringResource(R.string.image_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            onState = { failed = it is AsyncImagePainter.State.Error },
        )
        if (failed) {
            Icon(
                painter = painterResource(R.drawable.ic_broken_image),
                contentDescription = stringResource(R.string.image_load_error),
                modifier = Modifier
                    .size(48.dp)
                    .testTag(TestTags.MESSAGE_IMAGE_ERROR),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun formatTime(time: Long): String = remember(time) {
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(time))
}
