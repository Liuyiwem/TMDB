package com.yiwenliu.tmdb

import com.yiwenliu.feature.detail.api.navigation.MovieDetailNavKey
import com.yiwenliu.feature.home.api.navigation.HomeNavKey
import com.yiwenliu.tmdb.splash.SplashConfig
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class TestSplashConfig(override val isEnabled: Boolean = false) : SplashConfig

class MainViewModelTest {
    private val viewModel = MainViewModel(TestSplashConfig())

    @Test
    fun `onDeepLink keeps the pending stack when the next intent has no data`() {
        viewModel.onDeepLink("tmdb://movie?id=550")
        viewModel.onDeepLink(null)
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 550)),
            viewModel.deepLinkStack,
        )
    }

    @Test
    fun `onDeepLinkHandled clears the stack`() {
        viewModel.onDeepLink("tmdb://home")
        viewModel.onDeepLinkHandled()
        assertTrue(viewModel.deepLinkStack.isEmpty())
    }
}
