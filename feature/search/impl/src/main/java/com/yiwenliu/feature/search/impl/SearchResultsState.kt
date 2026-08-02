package com.yiwenliu.feature.search.impl

import androidx.paging.LoadState

/**
 * 搜尋結果區在任一時刻的狀態。
 *
 * 用 sealed interface 而不是幾個獨立的布林，是因為原本的寫法（`isSearching` 與 `isEmpty` 兩個
 * 布林）沒有把狀態空間切乾淨——五種情況用兩個布林表達，結果漏掉了「還沒輸入」這個沒被命名的
 * 狀態。窮盡的 `when` 讓這件事由編譯器保證：少一個分支就編不過。
 */
internal sealed interface SearchResultsState {
    /** 還沒輸入任何東西。刻意什麼都不畫。 */
    data object Idle : SearchResultsState

    data object Loading : SearchResultsState

    data class Error(val throwable: Throwable) : SearchResultsState

    data object Empty : SearchResultsState

    data object Results : SearchResultsState
}

/**
 * 把輸入狀態與 pager 狀態合成單一的畫面狀態。
 *
 * 抽成純函式（而不是寫在 composable 裡）的好處是這個切分本身可以用普通 JVM 測試覆蓋，
 * 不需要 Compose、模擬器或 paging 假物件。
 *
 * **分支順序是承重的**：
 * - `isPending` 必須排在 [SearchResultsState.Error] 與 `itemCount == 0` 之前。pending 期間
 *   pager 持的還是【上一個】查詢的狀態，可能是 `NotLoading` + 0 筆（會被誤判成 Empty），
 *   也可能是上一次的失敗。兩種都該顯示 loading。
 * - [SearchResultsState.Error] 必須排在 `itemCount == 0` 之前。refresh 失敗時 itemCount 必然
 *   是 0（每次改查詢都是全新的 Pager），順序寫反會把網路失敗顯示成「找不到結果」。
 */
internal fun searchResultsStateOf(
    isIdle: Boolean,
    isPending: Boolean,
    refresh: LoadState,
    itemCount: Int,
): SearchResultsState = when {
    isIdle -> SearchResultsState.Idle
    isPending || refresh is LoadState.Loading -> SearchResultsState.Loading
    refresh is LoadState.Error -> SearchResultsState.Error(refresh.error)
    itemCount == 0 -> SearchResultsState.Empty
    else -> SearchResultsState.Results
}
