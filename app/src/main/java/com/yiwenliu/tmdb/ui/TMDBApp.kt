package com.yiwenliu.tmdb.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.yiwenliu.feature.favorite.impl.navigation.favoriteEntry
import com.yiwenliu.feature.movie.impl.navigation.movieEntry
import com.yiwenliu.feature.search.impl.navigation.searchEntry
import com.yiwenliu.navigation.NavigationState
import com.yiwenliu.navigation.Navigator
import com.yiwenliu.navigation.toEntries
import com.yiwenliu.tmdb.navigation.TOP_LEVEL_NAV_ITEMS

@Composable
fun TMDBApp(
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val layoutType =
        if (
            windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
        ) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }

    val navigator = remember(navigationState) { Navigator(navigationState) }

    val entryProvider =
        entryProvider {
            movieEntry()
            searchEntry()
            favoriteEntry()
        }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                val selected = navKey == navigationState.currentTopLevelKey
                item(
                    selected = selected,
                    onClick = { navigator.navigate(navKey) },
                    icon = {
                        Icon(
                            imageVector =
                                if (selected) {
                                    navItem.selectedIcon
                                } else {
                                    navItem.unselectedIcon
                                },
                            contentDescription = stringResource(navItem.iconTextId),
                        )
                    },
                    label = { Text(stringResource(navItem.iconTextId)) },
                )
            }
        },
        modifier = modifier,
        layoutType = layoutType,
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
