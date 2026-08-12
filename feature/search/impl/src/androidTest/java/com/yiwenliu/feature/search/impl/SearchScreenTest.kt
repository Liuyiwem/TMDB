package com.yiwenliu.feature.search.impl

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.DataErrorException
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.core.ui.preview.MoviePreviewParameterProvider
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SearchScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleMovies: List<Movie> = MoviePreviewParameterProvider().values.first()

    private val settled = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )

    @Composable
    private fun Screen(
        state: SearchUiState,
        movies: List<Movie> = emptyList(),
        loadStates: LoadStates = settled,
        onAction: (SearchAction) -> Unit = {},
    ) {
        val items = flowOf(PagingData.from(movies, sourceLoadStates = loadStates))
            .collectAsLazyPagingItems()
        MaterialTheme {
            SearchScreen(state = state, searchMovies = items, onAction = onAction, onMovieClick = { _, _ -> })
        }
    }

    @Test
    fun blankQuery_showsNeitherEmptyNorLoading() {
        composeTestRule.setContent { Screen(state = SearchUiState(queryString = "")) }
        composeTestRule.onNodeWithTag(SearchTestTags.EMPTY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SearchTestTags.LOADING).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SearchTestTags.GRID).assertDoesNotExist()
    }

    @Test
    fun pendingQuery_showsLoadingNotEmpty() {
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = "b", isPending = true))
        }
        composeTestRule.onNodeWithTag(SearchTestTags.LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchTestTags.EMPTY).assertDoesNotExist()
    }

    @Test
    fun refreshLoading_showsLoadingIndicator() {
        composeTestRule.setContent {
            Screen(
                state = SearchUiState(queryString = "batman"),
                loadStates = settled.copy(refresh = LoadState.Loading),
            )
        }
        composeTestRule.onNodeWithTag(SearchTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun settledQueryWithNoResults_showsEmptyMessage() {
        composeTestRule.setContent { Screen(state = SearchUiState(queryString = "qxzqxz")) }
        composeTestRule.onNodeWithTag(SearchTestTags.EMPTY).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.search_no_results))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchTestTags.GRID).assertDoesNotExist()
    }

    @Test
    fun resultsPresent_showsGridWithFirstTitle() {
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = "batman"), movies = sampleMovies)
        }
        composeTestRule.onNodeWithTag(SearchTestTags.GRID).assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleMovies.first().title).assertIsDisplayed()
    }

    @Test
    fun refreshError_showsMappedMessageAndRetry() {
        composeTestRule.setContent {
            Screen(
                state = SearchUiState(queryString = "batman"),
                loadStates = settled.copy(
                    refresh = LoadState.Error(DataErrorException(DataError.Remote.NO_INTERNET)),
                ),
            )
        }
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.ui.R.string.error_no_internet),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.RETRY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchTestTags.EMPTY).assertDoesNotExist()
    }

    @Test
    fun appendLoading_showsIndicatorAtEndOfGrid() {
        composeTestRule.setContent {
            Screen(
                state = SearchUiState(queryString = "batman"),
                movies = sampleMovies,
                loadStates = settled.copy(append = LoadState.Loading),
            )
        }
        composeTestRule.onNodeWithTag(SearchTestTags.GRID)
            .performScrollToNode(hasTestTag(SearchTestTags.APPEND_LOADING))
        composeTestRule.onNodeWithTag(SearchTestTags.APPEND_LOADING).assertIsDisplayed()
    }

    @Test
    fun typing_emitsOnQueryStringChanged() {
        var lastAction: SearchAction? = null
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = ""), onAction = { lastAction = it })
        }
        composeTestRule.onNodeWithTag(SearchTestTags.TEXT_FIELD).performTextInput("batman")
        assertEquals(SearchAction.OnQueryStringChanged("batman"), lastAction)
    }

    @Test
    fun pastingTextWithNewlines_collapsesThemIntoSpaces() {
        var lastAction: SearchAction? = null
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = ""), onAction = { lastAction = it })
        }
        composeTestRule.onNodeWithTag(SearchTestTags.TEXT_FIELD).performTextInput("Fight\nClub")
        assertEquals(SearchAction.OnQueryStringChanged("Fight Club"), lastAction)
    }

    @Test
    fun clickingClearIcon_emitsEmptyQuery() {
        var lastAction: SearchAction? = null
        composeTestRule.setContent {
            Screen(state = SearchUiState(queryString = "batman"), onAction = { lastAction = it })
        }
        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.getString(R.string.close))
            .performClick()
        assertEquals(SearchAction.OnQueryStringChanged(""), lastAction)
    }
}
