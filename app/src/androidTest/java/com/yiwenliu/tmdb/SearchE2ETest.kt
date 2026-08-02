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

/**
 * 走完整條路徑的 E2E：
 * SearchTextField → SearchViewModel（debounce）→ SearchMoviesPagerUseCase →
 * FakeMovieRepository（真正的 Pager）→ MockTMDBApiService → JSON asset → MoviePagingGrid
 *
 * 只有 `:app` 能跑 Hilt 測試（只有它有 kspAndroidTest 與 core:data-test / core:testing）。
 */
@HiltAndroidTest
class SearchE2ETest {
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

        // debounce 是真實時間（300ms），而分頁載入還要讀 asset，所以必須等。
        composeTestRule.awaitText(FIGHT_CLUB)
        composeTestRule.onNodeWithText(FIGHT_CLUB).assertIsDisplayed()
        composeTestRule.onNodeWithTag("search:grid").assertIsDisplayed()
    }

    @Test
    fun searchTab_queryWithNoResults_showsEmptyState() {
        composeTestRule.onNodeWithText(searchTab).performClick()

        // MockTMDBApiService 對這個保留字串回傳 search_movies_empty.json。
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

        // 空查詢刻意留白：既不是結果網格，也不是「找不到結果」，更不該是永久轉圈。
        // 後者是 SearchViewModel 的 PagingData.empty(sourceLoadStates = ...) 在守著的。
        composeTestRule.onNodeWithTag("search:grid").assertDoesNotExist()
        composeTestRule.onNodeWithTag("search:empty").assertDoesNotExist()
        composeTestRule.onNodeWithTag("search:loading").assertDoesNotExist()
    }

    private companion object {
        /** search_movies.json 裡唯一的一筆。 */
        const val FIGHT_CLUB = "Fight Club"
    }
}
