package com.yiwenliu.tmdb

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText

private const val AWAIT_TIMEOUT_MILLIS = 5_000L

/**
 * 等待某段文字出現。
 *
 * E2E 測試不能直接 `assertIsDisplayed()`：分頁載入是非同步的（mock flavor 也一樣要讀 asset、
 * 過 Pager，搜尋還多了 debounce），斷言會在資料到達前就執行。
 */
internal fun ComposeTestRule.awaitText(text: String) {
    waitUntil(timeoutMillis = AWAIT_TIMEOUT_MILLIS) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

/** [awaitText] 的 testTag 版本。 */
internal fun ComposeTestRule.awaitTag(tag: String) {
    waitUntil(timeoutMillis = AWAIT_TIMEOUT_MILLIS) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
}
