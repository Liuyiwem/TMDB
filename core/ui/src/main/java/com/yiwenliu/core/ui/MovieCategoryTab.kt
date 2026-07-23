package com.yiwenliu.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yiwenliu.core.model.MovieCategory

@Composable
fun MovieCategoryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.testTag("tabRow"),
        containerColor = Color.Transparent,
        edgePadding = 16.dp,
        indicator = {},
        divider = {},
        tabs = tabs,
    )
}

@Composable
fun MovieCategoryTab(
    category: MovieCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = text,
        shape = RoundedCornerShape(percent = 50),
        modifier =
        modifier
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .testTag("tab:${category.name}"),
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun MovieCategoryTabRowPreview() {
    MaterialTheme {
        MovieCategoryTabRow(
            selectedTabIndex = MovieCategory.entries.indexOf(MovieCategory.POPULAR),
        ) {
            MovieCategory.entries.forEach { category ->
                MovieCategoryTab(
                    category = category,
                    selected = category == MovieCategory.POPULAR,
                    onClick = {},
                    text = { Text(category.name) },
                )
            }
        }
    }
}
