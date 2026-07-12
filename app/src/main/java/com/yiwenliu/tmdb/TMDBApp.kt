package com.yiwenliu.tmdb

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.yiwenliu.feature.movie.navigation.movieEntry
import com.yiwenliu.navigation.NavigationState
import com.yiwenliu.navigation.Navigator
import com.yiwenliu.navigation.toEntries

@Composable
fun TMDBApp(
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
) {
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val entryProvider =
        entryProvider {
            movieEntry()
        }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
