package com.yiwenliu.tmdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yiwenliu.feature.movie.api.navigation.MovieNavKey
import com.yiwenliu.navigation.rememberNavigationState
import com.yiwenliu.tmdb.navigation.TOP_LEVEL_NAV_ITEMS
import com.yiwenliu.tmdb.ui.TMDBApp
import com.yiwenliu.tmdb.ui.theme.TMDBTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TMDBTheme {
                val navigationState =
                    rememberNavigationState(
                        startKey = MovieNavKey,
                        topLevelKeys = TOP_LEVEL_NAV_ITEMS.keys,
                    )
                TMDBApp(navigationState = navigationState)
            }
        }
    }
}
