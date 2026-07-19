package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    private val searchMarker = "Search Screen"
    private val favoriteMarker = "Favorite Screen"
    private val homeMarkerTag = "movie:tabRow"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun selectSearchTab_showsSearchScreen() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule.onNodeWithText(searchMarker).assertIsDisplayed()
    }

    @Test
    fun selectFavoriteTab_showsFavoriteScreen() {
        composeTestRule.onNodeWithText(favoriteTab).performClick()
        composeTestRule.onNodeWithText(favoriteMarker).assertIsDisplayed()
    }

    @Test
    fun returnToHomeTab_showsHomeScreen() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule.onNodeWithText(searchMarker).assertIsDisplayed()

        composeTestRule.onNodeWithText(homeTab).performClick()
        composeTestRule.onNodeWithTag(homeMarkerTag).assertIsDisplayed()
    }
}
