package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.feature.favorite.impl.FavoriteTestTags
import com.yiwenliu.feature.search.impl.SearchTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TopLevelNavigationE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val homeTab get() = composeTestRule.activity.getString(R.string.feature_home)
    private val searchTab get() = composeTestRule.activity.getString(R.string.feature_search)
    private val favoriteTab get() = composeTestRule.activity.getString(R.string.feature_favorite)

    private val searchMarkerTag = SearchTestTags.TEXT_FIELD
    private val favoriteMarkerTag = FavoriteTestTags.EMPTY
    private val homeMarkerTag = TmdbTestTags.TAB_ROW

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun selectSearchTab_showsSearchScreen() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule.onNodeWithTag(searchMarkerTag).assertIsDisplayed()
    }

    @Test
    fun selectFavoriteTab_showsFavoriteScreen() {
        composeTestRule.onNodeWithText(favoriteTab).performClick()
        composeTestRule.awaitTag(favoriteMarkerTag)
        composeTestRule.onNodeWithTag(favoriteMarkerTag).assertIsDisplayed()
    }

    @Test
    fun returnToHomeTab_showsHomeScreen() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule.onNodeWithTag(searchMarkerTag).assertIsDisplayed()

        composeTestRule.onNodeWithText(homeTab).performClick()
        composeTestRule.onNodeWithTag(homeMarkerTag).assertIsDisplayed()
    }
}
