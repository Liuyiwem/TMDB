package com.yiwenliu.feature.search.impl

data class SearchUiState(
    val queryString: String = "",
    /**
     * 使用者已經輸入了新的查詢，但 pager 還沒開始載入它（還在 debounce 視窗內）。
     *
     * 這個旗標存在的唯一目的，是讓畫面在這段空窗期顯示 loading。少了它，`queryString` 已非
     * 空白而 pager 還停在上一份 `PagingData`（`refresh = NotLoading`、`itemCount = 0`），
     * 畫面會被判定成「沒有結果」——使用者打第一個字會先閃一下「No results found」才看到結果。
     */
    val isPending: Boolean = false,
) {
    /** 還沒輸入任何東西。刻意什麼都不顯示。 */
    val isIdle: Boolean get() = queryString.isBlank()
}
