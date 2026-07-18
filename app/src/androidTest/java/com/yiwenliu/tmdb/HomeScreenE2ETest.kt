package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val nowPlayingMovie = "Moana 2"
    private val popularMovie = "Deadpool & Wolverine"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun nowPlaying_loadFromMock_render() {
        composeTestRule.onNodeWithTag("movie:tabRow").assertIsDisplayed()
        awaitText(nowPlayingMovie)
        composeTestRule.onNodeWithText(nowPlayingMovie).assertIsDisplayed()
    }

    @Test
    fun selectPopularTab_loadsPopularMovies() {
        awaitText(nowPlayingMovie)

        composeTestRule.onNodeWithTag("movie:tab:POPULAR").performClick()

        awaitText(popularMovie)
        composeTestRule.onNodeWithText(popularMovie).assertIsDisplayed()
    }

    private fun awaitText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText(text)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
