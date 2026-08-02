package com.yiwenliu.tmdb.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.yiwenliu.core.navigation.NavigationState
import com.yiwenliu.core.navigation.Navigator
import com.yiwenliu.core.navigation.toEntries
import com.yiwenliu.core.ui.TmdbNavItem
import com.yiwenliu.feature.favorite.impl.navigation.favoriteEntry
import com.yiwenliu.feature.home.impl.navigation.homeEntry
import com.yiwenliu.feature.search.impl.navigation.searchEntry
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

    // 誰該避開輸入法，取決於導覽元件放在哪一邊。
    //
    // 底部列（寬度 < 600dp，例如手機直向）就在鍵盤正上方，所以整個 scaffold 需要
    // imePadding()，否則分頁會被鍵盤蓋住、點不到。
    //
    // 側邊 rail（寬度 >= 600dp，例如手機橫向）在左側，鍵盤永遠遮不到它。這時對整個
    // scaffold 套 imePadding() 只會讓 rail 依鍵盤高度憑空縮短，在鍵盤邊緣留下一條空白 ——
    // 改由內容自己避開鍵盤。
    val usesBottomBar = layoutType == NavigationSuiteType.NavigationBar
    val navigationSuiteModifier = if (usesBottomBar) modifier.imePadding() else modifier
    val contentModifier = if (usesBottomBar) Modifier else Modifier.imePadding()

    val entryProvider =
        entryProvider {
            homeEntry()
            searchEntry()
            favoriteEntry()
        }

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
        // 輸入法不屬於 systemBars，所以這一行以下的任何東西都無法對鍵盤做出反應。
        //
        // 必須加在 NavigationSuiteScaffold 上，而不是內容裡面：NavigationSuiteScaffoldLayout
        // 先量測 NavigationBar，再把內容槽放進剩下的空間，所以內容槽裡的
        // Modifier.imePadding() 或 Scaffold(contentWindowInsets = ...) 都在導覽列的下層 ——
        // 只會縮小內容，導覽列仍舊被鍵盤蓋住，使用者無法切換分頁。
        //
        // 用完整的 ime inset，不要試著扣掉 navigationBars：ime inset 本身已經包含導覽列區域
        // （IME 畫在它之上），而 NavigationBar 會自己處理 systemBars 的底部 padding。
        // 扣掉之後內容盒底邊會落在鍵盤上緣【之下】，導覽列就有一段被鍵盤壓住（實機驗證過）。
        modifier = navigationSuiteModifier,
        navigationSuiteType = layoutType,
    ) {
        Scaffold(modifier = Modifier.fillMaxSize().then(contentModifier)) { innerPadding ->
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
