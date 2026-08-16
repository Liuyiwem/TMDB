package com.yiwenliu.tmdb

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.yiwenliu.core.ui.TmdbTestTags
import com.yiwenliu.feature.detail.impl.DetailTestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DeepLinkE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val deepLinkIntent =
        Intent(Intent.ACTION_VIEW, "tmdb://movie?id=550".toUri())
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setPackage(ApplicationProvider.getApplicationContext<Context>().packageName)

    @get:Rule(order = 1)
    val composeTestRule =
        AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>(
            activityRule = ActivityScenarioRule(deepLinkIntent),
            activityProvider = { rule ->
                var activity: MainActivity? = null
                rule.scenario.onActivity { activity = it }
                checkNotNull(activity) { "MainActivity was not launched" }
            },
        )

    private val deepLinkedMovie = "Fight Club"

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun deepLinkToMovie_opensDetailWithTheLoadedTitle() {
        composeTestRule.awaitTag(DetailTestTags.CONTENT)
        composeTestRule.awaitAppBarTitle(deepLinkedMovie)
        composeTestRule.onNodeWithTag(DetailTestTags.CONTENT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertTextEquals(deepLinkedMovie)
    }

    @Test
    fun backFromDeepLinkedDetail_returnsToHome() {
        composeTestRule.awaitTag(DetailTestTags.CONTENT)
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_NAV_ICON).performClick()
        composeTestRule.awaitTag(TmdbTestTags.TAB_ROW)
        composeTestRule.onNodeWithTag(TmdbTestTags.TAB_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TmdbTestTags.APP_BAR_TITLE).assertDoesNotExist()
    }
}
