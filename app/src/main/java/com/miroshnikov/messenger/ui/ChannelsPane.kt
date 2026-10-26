package com.miroshnikov.messenger.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.miroshnikov.messenger.R
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ChannelsPane(
    state: ChannelsState,
    selectedChannel: String?,
    listState: LazyListState,
    onSelect: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        PaneHeader {
            HeaderTitle(stringResource(R.string.channels_title), TestTags.CHANNELS_TITLE)
        }
        when (state) {
            ChannelsState.Loading -> LoadingIndicator()
            ChannelsState.Error -> ErrorMessage(onRetry)
            is ChannelsState.Loaded ->
                if (state.names.isEmpty()) {
                    CenteredText(stringResource(R.string.no_channels), TestTags.EMPTY)
                } else {
                    ChannelsList(state.names, selectedChannel, listState, onSelect)
                }
        }
    }
}

@Composable
private fun ChannelsList(
    names: ImmutableList<String>,
    selectedChannel: String?,
    listState: LazyListState,
    onSelect: (String) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.CHANNELS_LIST),
    ) {
        items(names, key = { it }) { name ->
            ChannelItem(name = name, selected = name == selectedChannel, onClick = { onSelect(name) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun ChannelItem(name: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick)
            .testTag(TestTags.CHANNEL_ITEM),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
    )
}
