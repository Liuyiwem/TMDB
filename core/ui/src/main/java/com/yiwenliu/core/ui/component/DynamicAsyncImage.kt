package com.yiwenliu.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun DynamicAsyncImage(imageUrl: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(imagePlaceholderColor))
        return
    }
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            ImagePlaceholder {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        },
        error = {
            ImagePlaceholder()
        },
    )
}

private val imagePlaceholderColor: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest

@Composable
private fun ImagePlaceholder(content: @Composable BoxScope.() -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(imagePlaceholderColor),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Preview(showBackground = true, widthDp = 160, heightDp = 240)
@Composable
private fun DynamicAsyncImagePreview() {
    MaterialTheme {
        DynamicAsyncImage(
            imageUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
            contentDescription = "Deadpool & Wolverine",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(widthDp = 160, heightDp = 240)
@Composable
private fun DynamicAsyncImageDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        DynamicAsyncImage(
            imageUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
            contentDescription = "Deadpool & Wolverine",
            modifier = Modifier.fillMaxSize(),
        )
    }
}
