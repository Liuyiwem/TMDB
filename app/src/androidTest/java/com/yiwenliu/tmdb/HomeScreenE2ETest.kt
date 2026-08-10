package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.network.mock.MockTMDBApiService
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.feature.home.impl.HomeTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
class HomeScreenE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var apiService: MockTMDBApiService

    private val nowPlayingMovie = "Moana 2"
    private val popularMovie = "Deadpool & Wolverine"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        apiService.errorToThrow = null
    }

    @Test
    fun nowPlaying_loadFromMock_render() {
        composeTestRule.onNodeWithTag(TmdbTestTags.TAB_ROW).assertIsDisplayed()
        awaitText(nowPlayingMovie)
        composeTestRule.onNodeWithText(nowPlayingMovie).assertIsDisplayed()
    }

    @Test
    fun selectPopularTab_loadsPopularMovies() {
        awaitText(nowPlayingMovie)
        composeTestRule.onNodeWithTag(TmdbTestTags.tab(MovieCategory.POPULAR)).performClick()
        awaitText(popularMovie)
        composeTestRule.onNodeWithText(popularMovie).assertIsDisplayed()
    }

    @Test
    fun categorySwitchFailure_showsErrorAndKeepsTabs() {
        awaitText(nowPlayingMovie)
        apiService.errorToThrow = IOException("boom")

        composeTestRule.onNodeWithTag(TmdbTestTags.tab(MovieCategory.POPULAR)).performClick()
        composeTestRule.awaitTag(TmdbTestTags.ERROR)

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.common.R.string.error_no_internet),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(HomeTestTags.GRID).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TmdbTestTags.TAB_ROW).assertIsDisplayed()
    }

    @Test
    fun categorySwitchFailureRetry_recoversAndShowsMovies() {
        awaitText(nowPlayingMovie)
        apiService.errorToThrow = IOException("boom")
        composeTestRule.onNodeWithTag(TmdbTestTags.tab(MovieCategory.POPULAR)).performClick()
        composeTestRule.awaitTag(TmdbTestTags.ERROR)

        apiService.errorToThrow = null
        composeTestRule.onNodeWithTag(TmdbTestTags.RETRY).performClick()

        awaitText(popularMovie)
        composeTestRule.onNodeWithText(popularMovie).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.ERROR).assertDoesNotExist()
    }

    private fun awaitText(text: String) = composeTestRule.awaitText(text)
}
