package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.yiwenliu.core.network.mock.MockTMDBApiService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SearchE2ETest {
    @get:Rule(order = -1)
    val disableSplashRule = DisableSplashRule()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val searchTab get() = composeTestRule.activity.getString(R.string.feature_search)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun searchTab_typingQuery_showsResults() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule.onNodeWithTag("search:textField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search:textField").performTextInput("fight")
        composeTestRule.awaitText(FIGHT_CLUB)
        composeTestRule.onNodeWithText(FIGHT_CLUB).assertIsDisplayed()
        composeTestRule.onNodeWithTag("search:grid").assertIsDisplayed()
    }

    @Test
    fun searchTab_queryWithNoResults_showsEmptyState() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule
            .onNodeWithTag("search:textField")
            .performTextInput(MockTMDBApiService.EMPTY_RESULT_QUERY)
        composeTestRule.awaitTag("search:empty")
        composeTestRule.onNodeWithTag("search:empty").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search:grid").assertDoesNotExist()
    }

    @Test
    fun searchTab_beforeTyping_showsNeitherResultsNorEmptyState() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule.onNodeWithTag("search:textField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search:grid").assertDoesNotExist()
        composeTestRule.onNodeWithTag("search:empty").assertDoesNotExist()
        composeTestRule.onNodeWithTag("search:loading").assertDoesNotExist()
    }

    private companion object {
        const val FIGHT_CLUB = "Fight Club"
    }
}
