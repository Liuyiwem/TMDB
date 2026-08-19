package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.yiwenliu.core.network.mock.MockTmdbApiService
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.feature.search.impl.SearchTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
class SearchE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var apiService: MockTmdbApiService

    private val searchTab get() = composeTestRule.activity.getString(R.string.feature_search)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        apiService.errorToThrow = null
    }

    @Test
    fun searchTab_typingQuery_showsResults() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule.onNodeWithTag(SearchTestTags.TEXT_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchTestTags.TEXT_FIELD).performTextInput("fight")
        composeTestRule.awaitText(FIGHT_CLUB)
        composeTestRule.onNodeWithText(FIGHT_CLUB).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchTestTags.GRID).assertIsDisplayed()
    }

    @Test
    fun searchTab_queryWithNoResults_showsEmptyState() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        composeTestRule
            .onNodeWithTag(SearchTestTags.TEXT_FIELD)
            .performTextInput(MockTmdbApiService.EMPTY_RESULT_QUERY)
        composeTestRule.awaitTag(SearchTestTags.EMPTY)
        composeTestRule.onNodeWithTag(SearchTestTags.EMPTY).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchTestTags.GRID).assertDoesNotExist()
    }

    @Test
    fun searchFailureThenRetry_showsErrorThenRecovers() {
        composeTestRule.onNodeWithText(searchTab).performClick()
        apiService.errorToThrow = IOException("boom")
        composeTestRule.onNodeWithTag(SearchTestTags.TEXT_FIELD).performTextInput("fight")
        composeTestRule.awaitTag(TmdbTestTags.ERROR)
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.ui.R.string.error_no_internet),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchTestTags.GRID).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SearchTestTags.EMPTY).assertDoesNotExist()
        apiService.errorToThrow = null
        composeTestRule.onNodeWithTag(TmdbTestTags.RETRY).performClick()
        composeTestRule.awaitText(FIGHT_CLUB)
        composeTestRule.onNodeWithText(FIGHT_CLUB).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.ERROR).assertDoesNotExist()
    }

    private companion object {
        const val FIGHT_CLUB = "Fight Club"
    }
}
