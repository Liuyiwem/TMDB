package com.yiwenliu.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    val topLevelStack = rememberNavBackStack(startKey)
    val subStacks = topLevelKeys.associateWith { key -> rememberNavBackStack(key) }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}

@Stable
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }

    val topLevelKeys
        get() = subStacks.keys

    val currentSubStack: NavBackStack<NavKey>
        get() =
            subStacks[currentTopLevelKey]
                ?: error("Sub stack for $currentTopLevelKey does not exist")

    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}

@Composable
fun NavigationState.toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
    val decoratedEntries =
        subStacks.mapValues { (_, stack) ->
            val decorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    rememberViewModelStoreNavEntryDecorator<NavKey>(),
                )
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider,
            )
        }

    // 回傳普通 List 而不是 SnapshotStateList。
    //
    // 這個清單每次重組都重建一份，而且沒有任何人就地修改它——NavDisplay 只讀
    // （require(entries.isNotEmpty())、rememberSceneState(entries, …)、entries.size）。
    // 快照系統要有【寫入】才會發出失效通知，所以包成 SnapshotStateList 買不到任何可觀察性；
    // 何況那個實例沒被 remember，活不過記錄它讀取的那一次組合。
    //
    // 真正讓這個函式在返回堆疊變動時重跑的是 topLevelStack：NavBackStack 是
    // 「MutableList by base, StateObject by base」委派在一個 SnapshotStateList 上，
    // 所以下面這行的 flatMap 是一次真正的快照讀取。
    // （rememberDecoratedNavEntries 回傳的是 fastMap 產生的普通 ArrayList，不可觀察——
    // 那條路上的讀取點是它內部的 remember(backStack.toList())。）
    //
    // 而且拿掉它不只是省一次配置：navigation3 的 BackStackAwareLifecycleNavEntryDecorator
    // 對這個清單做 rememberUpdatedState(entries)，那是「結構相等才跳過寫入」。
    // SnapshotStateList 沒有覆寫 equals（識別相等），所以每次重組都會寫入那個 MutableState、
    // 連帶失效每個已組合 entry 的 lifecycle 包裝；換成 ArrayList 後改用結構相等比較，
    // 返回堆疊沒變就不寫入、不失效。
    return topLevelStack.flatMap { decoratedEntries[it] ?: emptyList() }
}
