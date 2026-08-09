package com.yiwenliu.tmdb

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import com.yiwenliu.core.ui.TmdbTestTags

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

internal fun ComposeTestRule.awaitAppBarTitle(title: String) {
    waitUntil(timeoutMillis = AWAIT_TIMEOUT_MILLIS) {
        onAllNodesWithTag(TmdbTestTags.APP_BAR_TITLE)
            .fetchSemanticsNodes()
            .any { node ->
                node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == title } == true
            }
    }
}
