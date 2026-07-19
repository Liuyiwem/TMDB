package com.yiwenliu.feature.home.impl

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleMovies: List<Movie> = MoviePreviewParameterProvider().values.first()

    @Test
    fun homeScreen_showsTabsAndMovies() {
        composeTestRule.setContent {
            val items = flowOf(PagingData.from(sampleMovies)).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(selectedCategory = MovieCategory.NOW_PLAYING),
                    movies = items,
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("movie:tabRow").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.tab_now_playing))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
    }

    @Test
    fun homeScreen_tabClick_invokesOnActionWithCategory() {
        var lastAction: HomeAction? = null
        composeTestRule.setContent {
            val items = flowOf(PagingData.from(sampleMovies)).collectAsLazyPagingItems()
            MaterialTheme {
                HomeScreen(
                    state = HomeUiState(selectedCategory = MovieCategory.NOW_PLAYING),
                    movies = items,
                    onAction = { lastAction = it },
                )
            }
        }

        composeTestRule.onNodeWithTag("movie:tab:TOP_RATED").performClick()
        assertEquals(HomeAction.OnCategorySelected(MovieCategory.TOP_RATED), lastAction)
    }

    @Test
    fun homeScreen_errorRefresh_showsRetry() {
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
                HomeScreen(
                    state = HomeUiState(),
                    movies = items,
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("movie:error").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.retry))
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_appendLoading_showsIndicator() {
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
                HomeScreen(
                    state = HomeUiState(),
                    movies = items,
                    onAction = {},
                )
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
