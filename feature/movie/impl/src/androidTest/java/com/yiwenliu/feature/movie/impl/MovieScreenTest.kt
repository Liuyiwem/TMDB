package com.yiwenliu.feature.movie.impl

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MovieScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleMovies: List<Movie> = MoviePreviewParameterProvider().values.first()

    @Test
    fun movieScreen_showsTitleAndMovies() {
        composeTestRule.setContent {
            val items = flowOf(PagingData.from(sampleMovies)).collectAsLazyPagingItems()
            MaterialTheme {
                MovieScreen(popularMovies = items, isRefreshing = false, onRefresh = {})
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.popular_movies),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
    }

    @Test
    fun movieScreen_errorRefresh_showsRetry() {
        val errorStates =
            LoadStates(
                refresh = LoadState.Error(RuntimeException("boom")),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            )
        composeTestRule.setContent {
            val items =
                flowOf(
                    PagingData.from(emptyList<Movie>(), sourceLoadStates = errorStates),
                ).collectAsLazyPagingItems()
            MaterialTheme {
                MovieScreen(popularMovies = items, isRefreshing = false, onRefresh = {})
            }
        }

        composeTestRule.onNodeWithTag("movie:error").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.retry),
            ).assertIsDisplayed()
    }

    @Test
    fun movieScreen_appendLoading_showsIndicator() {
        val appendLoading =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            )
        composeTestRule.setContent {
            val items =
                flowOf(
                    PagingData.from(sampleMovies, sourceLoadStates = appendLoading),
                ).collectAsLazyPagingItems()
            MaterialTheme {
                MovieScreen(popularMovies = items, isRefreshing = false, onRefresh = {})
            }
        }

        composeTestRule
            .onNodeWithTag("movie:grid")
            .performScrollToNode(hasTestTag("movie:appendLoading"))
        composeTestRule.onNodeWithTag("movie:appendLoading").assertIsDisplayed()
    }

    @Test
    fun errorItem_retryClick_invokesCallback() {
        var retried = false
        composeTestRule.setContent {
            MaterialTheme {
                ErrorItem(error = RuntimeException("boom"), onRetry = { retried = true })
            }
        }

        composeTestRule.onNodeWithTag("movie:retry").performClick()
        assertTrue(retried)
    }
}
