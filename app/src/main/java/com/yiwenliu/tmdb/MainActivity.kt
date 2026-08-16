package com.yiwenliu.tmdb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.yiwenliu.core.navigation.rememberNavigationState
import com.yiwenliu.core.ui.component.LottieSplashScreen
import com.yiwenliu.feature.home.api.navigation.HomeNavKey
import com.yiwenliu.tmdb.navigation.TOP_LEVEL_NAV_ITEMS
import com.yiwenliu.tmdb.ui.TmdbApp
import com.yiwenliu.tmdb.ui.theme.TmdbTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            viewModel.onDeepLink(intent?.data?.toString())
        }
        setContent {
            TmdbTheme {
                val showSplash = viewModel.isSplashVisible
                val navigationState =
                    rememberNavigationState(
                        startKey = HomeNavKey,
                        topLevelKeys = TOP_LEVEL_NAV_ITEMS.keys,
                    )

                val view = LocalView.current
                val darkTheme = isSystemInDarkTheme()
                LaunchedEffect(showSplash, darkTheme) {
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        !showSplash && !darkTheme
                }

                Box {
                    TmdbApp(
                        navigationState = navigationState,
                        deepLinkStack = viewModel.deepLinkStack,
                        onDeepLinkHandled = viewModel::onDeepLinkHandled,
                    )
                    if (showSplash) {
                        LottieSplashScreen(
                            animationRes = R.raw.splash,
                            backgroundColor = colorResource(R.color.splash_background),
                            onFinished = viewModel::onSplashFinished,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.onDeepLink(intent.data?.toString())
    }
}
