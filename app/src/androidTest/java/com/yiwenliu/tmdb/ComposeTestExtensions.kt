package com.yiwenliu.tmdb

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText

private const val AWAIT_TIMEOUT_MILLIS = 5_000L

internal fun ComposeTestRule.awaitText(text: String) {
    waitUntil(timeoutMillis = AWAIT_TIMEOUT_MILLIS) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

internal fun ComposeTestRule.awaitTag(tag: String) {
    waitUntil(timeoutMillis = AWAIT_TIMEOUT_MILLIS) {
        onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
}
