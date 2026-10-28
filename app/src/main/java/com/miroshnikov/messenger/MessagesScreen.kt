package com.miroshnikov.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.text.DateFormat
import java.util.Date

@Composable
fun MessagesScreen(
    chat: String,
    state: ChatUiState,
    isLandscape: Boolean,
    listState: LazyListState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(Modifier.heightIn(min = 56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                    if (isLandscape) {
                        Icon(painterResource(R.drawable.ic_close), stringResource(R.string.close_chat))
                    } else {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.back_to_channels))
                    }
                }
                Text(
                    text = chat,
                    modifier = Modifier.padding(horizontal = 12.dp).testTag("chat_title"),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        when {
            state.messages.isNotEmpty() -> MessagesList(state, listState, onLoadMore, onImageClick)
            state.messagesError -> ErrorWithRetry(onLoadMore)
            state.allLoaded -> CenteredText(stringResource(R.string.no_messages), "empty")
            else -> Loading()
        }
    }
}

@Composable
fun MessagesList(
    state: ChatUiState,
    listState: LazyListState,
    onLoadMore: () -> Unit,
    onImageClick: (String) -> Unit,
) {
    LazyColumn(
        state = listState,
        reverseLayout = true,
        modifier = Modifier.fillMaxSize().testTag("messages_list"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.messages, key = { it.id }) { message ->
            MessageItem(message, onImageClick)
        }
        if (state.messagesError) {
            item {
                Column(Modifier.fillMaxWidth().testTag("error"), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.load_error))
                    TextButton(onClick = onLoadMore, modifier = Modifier.testTag("retry")) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        } else if (!state.allLoaded) {
            item {
                LaunchedEffect(state.messages.size, onLoadMore) { onLoadMore() }
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.testTag("loading"))
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, onImageClick: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("message_item"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.from,
                    modifier = Modifier.weight(1f, fill = false).testTag("message_author"),
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
            val text = message.data.text
            val link = message.data.image?.link
            when {
                text != null -> Text(
                    text = text.text,
                    modifier = Modifier.padding(top = 4.dp).testTag("message_text"),
                    style = MaterialTheme.typography.bodyLarge,
                )
                link != null -> Thumbnail(link, onClick = { onImageClick(link) })
                else -> Text(
                    text = stringResource(R.string.unsupported_message),
                    modifier = Modifier.padding(top = 4.dp),
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
fun Thumbnail(link: String, onClick: () -> Unit) {
    var failed by remember(link) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .size(200.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .testTag("message_image"),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ChatServer.thumbnailUrl(link),
            contentDescription = stringResource(R.string.image_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            onState = { failed = it is AsyncImagePainter.State.Error },
        )
        if (failed) {
            Icon(
                painter = painterResource(R.drawable.ic_broken_image),
                contentDescription = stringResource(R.string.image_load_error),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun formatTime(time: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(time))
