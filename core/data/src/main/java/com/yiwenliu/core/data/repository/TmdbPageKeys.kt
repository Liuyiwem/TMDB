package com.yiwenliu.core.data.repository

/**
 * TMDB 會拒絕 `page` 大於 500 的請求（回 HTTP 400），但 `total_pages` 是**未裁切**回傳的
 * ——`movie/popular` 就會回報大約 42,000 頁。
 *
 * 所以只用 `total_pages` 當停止條件是不夠的：滑到第 500 頁之後會去請求第 501 頁，拿到 400，
 * 經 `safeCall` 映成 `NetworkError.CLIENT_ERROR`，最後在畫面底部變成一個**永遠重試不會成功**
 * 的錯誤項目。
 */
internal const val TMDB_MAX_PAGE = 500

/** 下一頁的 key，同時尊重 `total_pages` 與 TMDB 的 500 頁硬上限。 */
internal fun nextPageKeyOf(
    page: Int,
    totalPages: Int,
): Int? = if (page >= minOf(totalPages, TMDB_MAX_PAGE)) null else page + 1

/** 前一頁的 key。第一頁沒有前一頁，回傳 `null` 讓 Paging 知道 prepend 已到頂。 */
internal fun prevPageKeyOf(page: Int): Int? = if (page == 1) null else page - 1
