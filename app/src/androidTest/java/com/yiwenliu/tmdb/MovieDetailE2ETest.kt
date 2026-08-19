package com.yiwenliu.tmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.yiwenliu.core.network.mock.MockTmdbApiService
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.feature.detail.impl.DetailTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
class MovieDetailE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var apiService: MockTmdbApiService

    private val nowPlayingMovie = "Moana 2"
    private val recommendedMovie = "Inside Out 2"
    private val otherRecommendedMovie = "Fight Club"

    @Before
    fun setup() {
        hiltRule.inject()
        composeTestRule.awaitText(nowPlayingMovie)
    }

    @After
    fun tearDown() {
        apiService.errorToThrow = null
    }

    private fun openDetail() {
        composeTestRule.onNodeWithText(nowPlayingMovie).performClick()
        composeTestRule.awaitTag(DetailTestTags.CONTENT)
    }

    private fun scrollToRecommendations() {
        composeTestRule
            .onNodeWithTag(DetailTestTags.CONTENT)
            .performScrollToNode(hasTestTag(DetailTestTags.RECOMMENDATIONS))
    }

    @Test
    fun homeMovieClick_opensDetailWithTheListTitle() {
        openDetail()
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertTextEquals(nowPlayingMovie)
        composeTestRule.onNodeWithTag(DetailTestTags.CONTENT).assertIsDisplayed()
    }

    @Test
    fun backFromDetail_returnsToHome() {
        openDetail()
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_NAV_ICON).performClick()
        composeTestRule.awaitTag(TmdbTestTags.TAB_ROW)
        composeTestRule.onNodeWithTag(TmdbTestTags.TAB_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertDoesNotExist()
    }

    @Test
    fun recommendationClick_opensAnotherDetail() {
        openDetail()
        scrollToRecommendations()
        composeTestRule.onNodeWithText(recommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(recommendedMovie)
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertTextEquals(recommendedMovie)
    }

    @Test
    fun backFromNestedDetail_returnsToTheFirstDetail() {
        openDetail()
        scrollToRecommendations()
        composeTestRule.onNodeWithText(recommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(recommendedMovie)
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_NAV_ICON).performClick()
        composeTestRule.awaitAppBarTitle(nowPlayingMovie)
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertTextEquals(nowPlayingMovie)
    }

    @Test
    fun revisitDetailAlreadyInBackStack_keepsItsTitle() {
        openDetail()
        scrollToRecommendations()
        composeTestRule.onNodeWithText(recommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(recommendedMovie)
        scrollToRecommendations()
        composeTestRule.onNodeWithText(otherRecommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(otherRecommendedMovie)
        scrollToRecommendations()
        composeTestRule.onNodeWithText(recommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(recommendedMovie)
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertTextEquals(recommendedMovie)
    }

    @Test
    fun backFromRevisitedDetail_returnsToThePreviousDetail() {
        openDetail()
        scrollToRecommendations()
        composeTestRule.onNodeWithText(recommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(recommendedMovie)
        scrollToRecommendations()
        composeTestRule.onNodeWithText(otherRecommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(otherRecommendedMovie)
        scrollToRecommendations()
        composeTestRule.onNodeWithText(recommendedMovie).performClick()
        composeTestRule.awaitAppBarTitle(recommendedMovie)
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_NAV_ICON).performClick()
        composeTestRule.awaitAppBarTitle(otherRecommendedMovie)
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertTextEquals(otherRecommendedMovie)
    }

    @Test
    fun detailLoadFailure_showsErrorDialogWithoutContent() {
        apiService.errorToThrow = IOException("boom")
        composeTestRule.onNodeWithText(nowPlayingMovie).performClick()
        composeTestRule.awaitTag(TmdbTestTags.MESSAGE_DIALOG)
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(com.yiwenliu.core.ui.R.string.error_no_internet),
            ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DetailTestTags.CONTENT).assertDoesNotExist()
    }

    @Test
    fun detailErrorDialogConfirm_returnsToHome() {
        apiService.errorToThrow = IOException("boom")
        composeTestRule.onNodeWithText(nowPlayingMovie).performClick()
        composeTestRule.awaitTag(TmdbTestTags.MESSAGE_DIALOG)
        composeTestRule.onNodeWithTag(TmdbTestTags.MESSAGE_DIALOG_CONFIRM).performClick()
        composeTestRule.awaitTag(TmdbTestTags.TAB_ROW)
        composeTestRule.onNodeWithTag(TmdbTestTags.TAB_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.MESSAGE_DIALOG).assertDoesNotExist()
    }
}
