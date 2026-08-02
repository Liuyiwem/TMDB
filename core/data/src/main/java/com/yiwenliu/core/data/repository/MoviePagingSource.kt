package com.yiwenliu.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.data.networking.safeCall
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * 所有 TMDB 電影清單共用的分頁來源。分類與搜尋的差別只有 [fetchPage] 這一個函式。
 */
internal class MoviePagingSource(
    private val ioDispatcher: CoroutineDispatcher,
    private val fetchPage: suspend (page: Int) -> MovieResponse,
) : PagingSource<Int, Movie>() {
    /**
     * 已呈現過的電影 id，用來過濾跨頁重複。
     *
     * **為什麼需要**：TMDB 會在兩次請求之間重新排序（搜尋的相關性排序尤其明顯，熱門清單也會
     * 隨時間變動），所以同一個 id 可能同時出現在第 N 頁與第 N+1 頁。而 UI 用
     * `itemKey { it.id }` 當 LazyVerticalGrid 的 key，Compose 的 lazy layout 遇到重複 key
     * 會直接拋例外。少了這個過濾就是一個真實的崩潰。
     *
     * **為什麼放在這裡是正確的**：Paging 為每個 generation（初次載入、每次 refresh／
     * invalidate）都會建一個全新的 [MoviePagingSource] 實例，所以這個集合的生命週期剛好等於
     * 一個 generation。下拉重新整理會拿到空集合，不會把新的第一頁誤判成重複。
     *
     * **不要在 PagingConfig 加上 `maxSize`**。一旦設了，Paging 會丟棄離螢幕太遠的頁，之後
     * 重新載入時會撞上這個已填充的集合、回傳空頁，項目就會無聲消失。若哪天真的需要
     * `maxSize`，必須先把去重改成不依賴實例狀態的做法（例如在 repository 對每份
     * `PagingData` 各套一次 `PagingData.filter`），而且要非常小心那個集合的宣告位置——
     * 提到 `map` lambda 外面就會跨 generation 共用，refresh 後整個第一頁都會被濾掉。
     */
    private val seenIds = mutableSetOf<Int>()

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return when (val result = withContext(ioDispatcher) { safeCall { fetchPage(page) } }) {
            is Result.Success -> {
                val movies =
                    result.data.results
                        .map { it.asExternalModel() }
                        .filter { seenIds.add(it.id) }
                LoadResult.Page(
                    data = movies,
                    prevKey = prevPageKeyOf(page),
                    nextKey = nextPageKeyOf(page, result.data.totalPages),
                )
            }

            is Result.Error -> LoadResult.Error(NetworkException(result.error))
        }
    }
}
