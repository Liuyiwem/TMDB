package com.yiwenliu.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

@Composable
fun DynamicAsyncImage(imageUrl: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceContainerHighest
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(placeholderColor))
        return
    }
    val placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) }
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
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
