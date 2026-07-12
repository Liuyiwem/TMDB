package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.yiwenliu.feature.movie.impl.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MovieScreenE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    val firstMovie = "Deadpool & Wolverine"
    val secondMovie = "Inside Out 2"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun popularMovies_loadFromMock_render() {
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.popular_movies),
            ).assertIsDisplayed()
        awaitFirstMovie()
        composeTestRule.onNodeWithText(firstMovie).assertIsDisplayed()
    }

    @Test
    fun popularMovies_scroll_revealsMoreItems() {
        awaitFirstMovie()
        composeTestRule
            .onNodeWithTag("movie:grid")
            .performScrollToNode(hasText(secondMovie))
        composeTestRule.onNodeWithText(secondMovie).assertIsDisplayed()
    }

    private fun awaitFirstMovie() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText(firstMovie)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
