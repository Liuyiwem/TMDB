package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.feature.detail.impl.DetailTestTags
import com.yiwenliu.feature.favorite.impl.FavoriteTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class FavoriteE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val movieTitle = "Moana 2"
    private val movieId = 1241982
    private val favoriteTab get() = composeTestRule.activity.getString(R.string.feature_favorite)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun favoriteFromDetail() {
        composeTestRule.awaitText(movieTitle)
        composeTestRule.onNodeWithText(movieTitle).performClick()
        composeTestRule.awaitTag(DetailTestTags.FAVORITE)
        composeTestRule.onNodeWithTag(DetailTestTags.FAVORITE).performClick()
        composeTestRule.awaitToggleOn(DetailTestTags.FAVORITE)
        composeTestRule.onNodeWithText(favoriteTab).performClick()
        composeTestRule.awaitTag(FavoriteTestTags.GRID)
    }

    @Test
    fun favoritingFromDetail_showsTheMovieInFavorites() {
        favoriteFromDetail()
        composeTestRule.onNodeWithTag(FavoriteTestTags.item(movieId)).assertIsDisplayed()
        composeTestRule.onNodeWithText(movieTitle).assertIsDisplayed()
    }

    @Test
    fun favoritesTab_startsEmpty() {
        composeTestRule.onNodeWithText(favoriteTab).performClick()
        composeTestRule.awaitTag(FavoriteTestTags.EMPTY)
        composeTestRule.onNodeWithTag(FavoriteTestTags.EMPTY).assertIsDisplayed()
    }

    @Test
    fun removingFromFavorites_requiresConfirmation() {
        favoriteFromDetail()
        composeTestRule.onNodeWithTag(FavoriteTestTags.remove(movieId)).performClick()
        composeTestRule.awaitTag(TmdbTestTags.CONFIRM_DIALOG)
        composeTestRule.onNodeWithTag(TmdbTestTags.CONFIRM_DIALOG_DISMISS).performClick()
        composeTestRule.onNodeWithTag(FavoriteTestTags.GRID).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FavoriteTestTags.remove(movieId)).performClick()
        composeTestRule.awaitTag(TmdbTestTags.CONFIRM_DIALOG)
        composeTestRule.onNodeWithTag(TmdbTestTags.CONFIRM_DIALOG_CONFIRM).performClick()
        composeTestRule.awaitTag(FavoriteTestTags.EMPTY)
    }
}
