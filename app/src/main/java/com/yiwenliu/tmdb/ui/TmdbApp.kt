package com.yiwenliu.tmdb.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.yiwenliu.core.navigation.NavigationState
import com.yiwenliu.core.navigation.Navigator
import com.yiwenliu.core.navigation.resolveTitleOverride
import com.yiwenliu.core.navigation.toEntries
import com.yiwenliu.core.navigation.topAppBarSpec
import com.yiwenliu.core.ui.LocalSnackbarHostState
import com.yiwenliu.core.ui.LocalTopAppBarTitleOverrides
import com.yiwenliu.core.ui.component.TmdbNavItem
import com.yiwenliu.core.ui.component.TmdbTopAppBar
import com.yiwenliu.core.ui.rememberTopAppBarTitleOverrides
import com.yiwenliu.feature.detail.impl.navigation.movieDetailEntry
import com.yiwenliu.feature.favorite.impl.navigation.favoriteEntry
import com.yiwenliu.feature.home.impl.navigation.homeEntry
import com.yiwenliu.feature.search.impl.navigation.searchEntry
import com.yiwenliu.tmdb.navigation.TOP_LEVEL_NAV_ITEMS

@Composable
fun TmdbApp(
    navigationState: NavigationState,
    deepLinkStack: List<NavKey>,
    onDeepLinkHandled: () -> Unit,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val titleOverrides = rememberTopAppBarTitleOverrides()
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val navigationSuiteType = windowAdaptiveInfo.navigationSuiteType
    val usesBottomBar = navigationSuiteType == NavigationSuiteType.NavigationBar

    LaunchedEffect(deepLinkStack) {
        if (deepLinkStack.isNotEmpty()) {
            navigator.replaceStack(deepLinkStack)
            onDeepLinkHandled()
        }
    }

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState,
        LocalTopAppBarTitleOverrides provides titleOverrides,
    ) {
        NavigationSuiteScaffold(
            navigationItems = {
                TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                    TmdbNavItem(
                        selected = navKey == navigationState.currentTopLevelKey,
                        onClick = { navigator.navigate(navKey) },
                        icon = navItem.icon,
                        label = stringResource(navItem.iconTextId),
                    )
                }
            },
            modifier = if (usesBottomBar) modifier.imePadding() else modifier,
            navigationSuiteType = navigationSuiteType,
        ) {
            TmdbAppContent(
                navigationState = navigationState,
                navigator = navigator,
                modifier = if (usesBottomBar) Modifier else Modifier.imePadding(),
            )
        }
    }
}

@Composable
private fun TmdbAppContent(navigationState: NavigationState, navigator: Navigator, modifier: Modifier = Modifier) {
    val snackbarHostState = LocalSnackbarHostState.current
    val titleOverrides = LocalTopAppBarTitleOverrides.current
    val entryProvider =
        entryProvider {
            homeEntry(navigator)
            searchEntry(navigator)
            favoriteEntry(navigator)
            movieDetailEntry(navigator)
        }
    val entries = navigationState.toEntries(entryProvider)
    val topAppBarSpec = entries.lastOrNull()?.topAppBarSpec

    Scaffold(
        modifier = Modifier.fillMaxSize().then(modifier),
        topBar = {
            topAppBarSpec?.let { spec ->
                TmdbTopAppBar(
                    title = resolveTitleOverride(navigationState.currentKey, titleOverrides)
                        ?: spec.titleRes?.let { stringResource(it) }.orEmpty(),
                    navIcon = spec.navIcon,
                    onNavIconClick = { if (navigator.canGoBack) navigator.goBack() },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavDisplay(
            entries = entries,
            onBack = { if (navigator.canGoBack) navigator.goBack() },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

private val WindowAdaptiveInfo.navigationSuiteType: NavigationSuiteType
    get() = if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        NavigationSuiteType.NavigationRail
    } else {
        NavigationSuiteType.NavigationBar
    }
