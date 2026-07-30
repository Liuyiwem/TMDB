package com.yiwenliu.tmdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.yiwenliu.core.navigation.rememberNavigationState
import com.yiwenliu.core.ui.LottieSplashScreen
import com.yiwenliu.feature.home.api.navigation.HomeNavKey
import com.yiwenliu.tmdb.navigation.TOP_LEVEL_NAV_ITEMS
import com.yiwenliu.tmdb.ui.TMDBApp
import com.yiwenliu.tmdb.ui.theme.TMDBTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TMDBTheme {
                val showSplash = splashEnabled && !splashViewModel.isFinished
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
                    TMDBApp(navigationState = navigationState)
                    if (showSplash) {
                        LottieSplashScreen(
                            animationRes = R.raw.splash,
                            backgroundColor = colorResource(R.color.splash_background),
                            onFinished = splashViewModel::onSplashFinished,
                        )
                    }
                }
            }
        }
    }

    companion object {
        @VisibleForTesting
        var splashEnabled = true
    }
}
