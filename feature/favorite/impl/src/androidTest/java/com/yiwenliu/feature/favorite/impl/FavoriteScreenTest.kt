package com.yiwenliu.feature.favorite.impl

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yiwenliu.core.model.FavoriteMovie
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.core.ui.preview.FavoriteMoviePreviewParameterProvider
import com.yiwenliu.core.ui.util.UiText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class FavoriteScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val favorites: List<FavoriteMovie> = FavoriteMoviePreviewParameterProvider().values.first()

    private var lastAction: FavoriteAction? = null

    private var clickedMovie: Pair<Int, String>? = null

    private fun renderScreen(state: FavoriteUiState) {
        composeTestRule.setContent {
            MaterialTheme {
                FavoriteScreen(
                    state = state,
                    onAction = { lastAction = it },
                    onMovieClick = { id, title -> clickedMovie = id to title },
                )
            }
        }
    }

    @Test
    fun loadingState_showsProgressIndicator() {
        renderScreen(FavoriteUiState())
        composeTestRule.onNodeWithTag(FavoriteTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun emptyFavorites_showsEmptyState() {
        renderScreen(FavoriteUiState(isLoading = false))
        composeTestRule.onNodeWithTag(FavoriteTestTags.EMPTY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FavoriteTestTags.GRID).assertDoesNotExist()
    }

    @Test
    fun favorites_areShownInTheGrid() {
        renderScreen(FavoriteUiState(isLoading = false, favorites = favorites))
        composeTestRule.onNodeWithTag(FavoriteTestTags.GRID).assertIsDisplayed()
        composeTestRule.onNodeWithText(favorites.first().title).assertIsDisplayed()
    }

    @Test
    fun itemClick_emitsOnMovieClick() {
        renderScreen(FavoriteUiState(isLoading = false, favorites = favorites))
        val movie = favorites.first()
        composeTestRule.onNodeWithTag(FavoriteTestTags.item(movie.id)).performClick()
        assertEquals(movie.id to movie.title, clickedMovie)
    }

    @Test
    fun favoriteIconClick_emitsOnRemoveClick() {
        renderScreen(FavoriteUiState(isLoading = false, favorites = favorites))
        val movie = favorites.first()
        composeTestRule.onNodeWithTag(FavoriteTestTags.remove(movie.id)).performClick()
        assertEquals(FavoriteAction.OnRemoveClick(movie), lastAction)
    }

    @Test
    fun pendingRemoval_showsDialogAndConfirmEmitsAction() {
        renderScreen(
            FavoriteUiState(
                isLoading = false,
                favorites = favorites,
                pendingRemoval = favorites.first(),
            ),
        )
        composeTestRule.onNodeWithTag(TmdbTestTags.CONFIRM_DIALOG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.CONFIRM_DIALOG_CONFIRM).performClick()
        assertEquals(FavoriteAction.OnRemoveConfirm, lastAction)
    }

    @Test
    fun error_showsMessageDialogOverTheGrid() {
        renderScreen(
            FavoriteUiState(
                isLoading = false,
                favorites = favorites,
                error = UiText.StringResource(com.yiwenliu.core.ui.R.string.error_disk_full),
            ),
        )
        composeTestRule.onNodeWithTag(TmdbTestTags.MESSAGE_DIALOG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FavoriteTestTags.GRID).assertIsDisplayed()
    }

    @Test
    fun errorDialogConfirm_emitsOnErrorDismiss() {
        renderScreen(
            FavoriteUiState(
                isLoading = false,
                favorites = favorites,
                error = UiText.StringResource(com.yiwenliu.core.ui.R.string.error_disk_full),
            ),
        )
        composeTestRule.onNodeWithTag(TmdbTestTags.MESSAGE_DIALOG_CONFIRM).performClick()
        assertEquals(FavoriteAction.OnErrorDismiss, lastAction)
    }

    @Test
    fun pendingRemoval_dismissEmitsAction() {
        renderScreen(
            FavoriteUiState(
                isLoading = false,
                favorites = favorites,
                pendingRemoval = favorites.first(),
            ),
        )
        composeTestRule.onNodeWithTag(TmdbTestTags.CONFIRM_DIALOG_DISMISS).performClick()
        assertEquals(FavoriteAction.OnRemoveDismiss, lastAction)
    }
}
