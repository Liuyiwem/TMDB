package com.yiwenliu.feature.detail.impl

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.ui.CastPreviewParameterProvider
import com.yiwenliu.core.ui.MovieDetailPreviewParameterProvider
import com.yiwenliu.core.ui.MoviePreviewParameterProvider
import com.yiwenliu.core.ui.TmdbTestTags
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MovieDetailScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleDetail = MovieDetailPreviewParameterProvider().values.first()
    private val sampleCast = CastPreviewParameterProvider().values.first()
    private val sampleRecommendations = MoviePreviewParameterProvider().values.first()

    private fun loadedState(
        isFavorite: Boolean = false,
        cast: List<CastMember> = sampleCast,
        recommendations: List<Movie> = sampleRecommendations,
    ) = MovieDetailUiState(
        isLoading = false,
        detail = sampleDetail,
        cast = cast,
        recommendations = recommendations,
        isFavorite = isFavorite,
    )

    private fun renderScreen(
        state: MovieDetailUiState,
        onAction: (MovieDetailAction) -> Unit = {},
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                MovieDetailScreen(state = state, onAction = onAction)
            }
        }
    }

    private fun scrollTo(tag: String) {
        composeTestRule
            .onNodeWithTag(DetailTestTags.CONTENT)
            .performScrollToNode(hasTestTag(tag))
    }

    @Test
    fun loadingState_showsSpinnerAndNoContent() {
        renderScreen(MovieDetailUiState())
        composeTestRule.onNodeWithTag(DetailTestTags.LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DetailTestTags.CONTENT).assertDoesNotExist()
    }

    @Test
    fun errorState_showsMappedMessage() {
        renderScreen(MovieDetailUiState(isLoading = false, error = NetworkError.NO_INTERNET))
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.common.R.string.error_no_internet),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DetailTestTags.CONTENT).assertDoesNotExist()
    }

    @Test
    fun errorState_retryClick_emitsOnRetry() {
        var lastAction: MovieDetailAction? = null
        renderScreen(
            MovieDetailUiState(isLoading = false, error = NetworkError.NO_INTERNET),
            onAction = { lastAction = it },
        )
        composeTestRule.onNodeWithTag(TmdbTestTags.RETRY).performClick()
        assertEquals(MovieDetailAction.OnRetry, lastAction)
    }

    @Test
    fun content_showsTaglineAndOverview() {
        renderScreen(loadedState())
        composeTestRule.onNodeWithText(sampleDetail.tagline).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.overview))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleDetail.overview).assertIsDisplayed()
    }

    @Test
    fun content_showsRuntimeAndGenresInTheMetaLine() {
        renderScreen(loadedState())
        val runtime =
            composeTestRule.activity.getString(R.string.runtime_minutes, sampleDetail.runtimeMinutes)
        val expected =
            "${sampleDetail.releaseDate} · $runtime · ${sampleDetail.genres.joinToString { it.name }}"
        composeTestRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun content_showsCastNamesAndCharacters() {
        renderScreen(loadedState())
        scrollTo(DetailTestTags.CAST)
        val first = sampleCast.first()
        composeTestRule.onNodeWithText(first.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(first.character).assertIsDisplayed()
    }

    @Test
    fun emptyCast_hidesTheCastSection() {
        renderScreen(loadedState(cast = emptyList()))
        composeTestRule.onNodeWithTag(DetailTestTags.CAST).assertDoesNotExist()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.cast))
            .assertDoesNotExist()
    }

    @Test
    fun emptyRecommendations_hidesTheRecommendationsSection() {
        renderScreen(loadedState(recommendations = emptyList()))
        composeTestRule.onNodeWithTag(DetailTestTags.RECOMMENDATIONS).assertDoesNotExist()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.recommendations))
            .assertDoesNotExist()
    }

    @Test
    fun recommendationClick_emitsIdAndTitle() {
        var lastAction: MovieDetailAction? = null
        renderScreen(loadedState(), onAction = { lastAction = it })
        scrollTo(DetailTestTags.RECOMMENDATIONS)
        val first = sampleRecommendations.first()
        composeTestRule.onNodeWithText(first.title).performClick()
        assertEquals(MovieDetailAction.OnRecommendationClick(first.id, first.title), lastAction)
    }

    @Test
    fun favoriteToggle_emitsCheckedTrue() {
        var lastAction: MovieDetailAction? = null
        renderScreen(loadedState(), onAction = { lastAction = it })
        composeTestRule.onNodeWithTag(DetailTestTags.FAVORITE).assertIsOff()
        composeTestRule.onNodeWithTag(DetailTestTags.FAVORITE).performClick()
        assertEquals(MovieDetailAction.OnFavoriteToggle(true), lastAction)
    }

    @Test
    fun favoriteChecked_showsRemoveContentDescriptionAndOnState() {
        renderScreen(loadedState(isFavorite = true))
        composeTestRule.onNodeWithTag(DetailTestTags.FAVORITE).assertIsOn()
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(R.string.remove_from_favorites),
            ).assertIsDisplayed()
    }
}
