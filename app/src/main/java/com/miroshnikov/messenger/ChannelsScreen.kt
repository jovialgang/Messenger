package com.miroshnikov.messenger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ChannelsScreen(
    state: ChatUiState,
    listState: LazyListState,
    onChannelClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer) {
            Text(
                text = stringResource(R.string.channels_title),
                modifier = Modifier.heightIn(min = 56.dp).padding(16.dp).testTag("channels_title"),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        when {
            state.channelsLoading -> Loading()
            state.channelsError -> ErrorWithRetry(onRetry)
            state.channels.isEmpty() -> CenteredText(stringResource(R.string.no_channels), "empty")
            else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize().testTag("channels_list")) {
                items(state.channels, key = { it }) { name ->
                    ChannelItem(name, selected = name == state.chat, onClick = { onChannelClick(name) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ChannelItem(name: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    ListItem(
        headlineContent = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier.selectable(selected = selected, onClick = onClick).testTag("channel_item"),
        colors = ListItemDefaults.colors(containerColor = background),
    )
}
