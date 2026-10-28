package com.miroshnikov.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

@Composable
fun ImageScreen(link: String, contentPadding: PaddingValues, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black).testTag("image_screen")) {
        SubcomposeAsyncImage(
            model = ChatServer.imageUrl(link),
            contentDescription = stringResource(R.string.image_description),
            modifier = Modifier.fillMaxSize().padding(contentPadding).testTag("full_image"),
            contentScale = ContentScale.Fit,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.testTag("loading"))
                }
            },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.image_load_error), color = Color.White)
                }
            },
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(contentPadding).padding(8.dp).testTag("close_image"),
        ) {
            Icon(painterResource(R.drawable.ic_close), stringResource(R.string.close_image), tint = Color.White)
        }
    }
}
