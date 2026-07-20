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
fun MovieCategoryTabs(
    categories: List<MovieCategory>,
    titles: List<String>,
    selectedCategory: MovieCategory,
    onCategorySelected: (MovieCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.testTag("tabRow"),
        containerColor = Color.Transparent,
        edgePadding = 16.dp,
        indicator = {},
        divider = {},
    ) {
        categories.forEachIndexed { index, category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(titles[index]) },
                shape = RoundedCornerShape(percent = 50),
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .testTag("tab:${category.name}"),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun MovieCategoryTabsPreview() {
    MaterialTheme {
        MovieCategoryTabs(
            categories = MovieCategory.entries,
            titles = MovieCategory.entries.map { it.name },
            selectedCategory = MovieCategory.POPULAR,
            onCategorySelected = {},
        )
    }
}
