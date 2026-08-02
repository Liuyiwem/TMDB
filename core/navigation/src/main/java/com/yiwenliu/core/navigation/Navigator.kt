package com.yiwenliu.core.navigation

import androidx.navigation3.runtime.NavKey

class Navigator(
    val state: NavigationState,
) {
    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    /**
     * 現在這個位置還能不能往回。false 代表就站在 [NavigationState.startKey]——
     * 返回的語意是離開 app，不是導覽。
     *
     * 為什麼需要它：NavDisplay 的返回處理看起來已經擋掉了這種情況
     * （isBackEnabled = scene.previousEntries.isNotEmpty()），但它的 body 是
     * repeat(entries.size - scene.previousEntries.size) { onBack() }，而 AOSP 原始碼註明
     * enabled 在同一個 frame 內派發手勢時可能已經過期。快速連按兩次返回真的碰得到
     * [goBack] 裡的 error()。呼叫端請先問這個。
     */
    val canGoBack: Boolean
        get() = state.currentKey != state.startKey

    fun goBack() {
        when (state.currentKey) {
            // 保留這個 error()：它是真正的不變量偵測器——走到它代表「startKey 永遠在
            // topLevelStack 底部」已被導覽 bug 破壞，你會希望它大聲。靜默返回會讓堆疊損毀
            // 表現成「返回鍵沒反應」，更難追。NavigatorTest 的
            // `goBack on empty stack throws` 釘著它。
            state.startKey -> error("You cannot go back from the start route")

            state.currentTopLevelKey -> state.topLevelStack.removeLastOrNull()

            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            remove(key)
            add(key)
        }
    }

    /**
     * 切換 top-level。回到 [NavigationState.startKey] 與切到其他分頁是刻意不對稱的。
     *
     * startKey 必須永遠是 topLevelStack 的第 0 個、而且只出現一次——[goBack] 的終止條件
     * 靠它。如果這裡對 startKey 也用 remove+add，Home→Search→Home 會留下 [Search, Home]，
     * 連按兩次返回就把 stack 清空，[NavigationState.currentTopLevelKey] 的 last() 直接拋
     * NoSuchElementException。clear() 不是懶惰，是那個不變量。
     *
     * 順帶一提這也剛好是 Android 底部導覽的慣例：回到起始分頁 = 收掉跨分頁的返回歷史。
     */
    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
