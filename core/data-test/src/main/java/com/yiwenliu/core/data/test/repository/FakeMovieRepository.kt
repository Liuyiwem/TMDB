package com.yiwenliu.core.data.test.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.data.networking.safeCall
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.network.mock.MockTMDBApiService
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 走真正的 [Pager] 與 [PagingSource]，而不是用裸 `flow { emit(PagingData.from(...)) }`。
 *
 * 這件事很重要，因為裸 flow 的失敗行為與生產環境完全不同：`cachedIn` 的終點是
 * `shareIn(scope, Lazily, replay = 1)`，而 `SharedFlow` 不會把例外傳給下游，所以從 flow 裡
 * 拋出的錯誤會拆掉 `viewModelScope`、直接殺掉受測 app，而不是變成 `LoadState.Error`。
 * `PagingData.from` 也會讓 `retry()` 變成 no-op。兩者加起來，畫面上的錯誤／重試 UI 在
 * instrumentation 測試裡永遠無法到達。
 */
internal class FakeMovieRepository
@Inject
constructor(
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val datasource: MockTMDBApiService,
) : MovieRepository {
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = pager { page ->
        datasource.getMoviesByCategory(category.path, page)
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = pager { page ->
        datasource.searchMovies(queryString, page)
    }

    private fun pager(fetchPage: suspend (page: Int) -> MovieResponse): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { FakePagingSource(ioDispatcher, fetchPage) },
    ).flow
}

/**
 * 刻意在這裡重新實作一份，而不是重用 `core:data` 的 PagingSource ——
 * 那個類別是 `internal`，把它放寬只為了測試假物件會把 `core:network` 的 DTO
 * 洩漏進 `core:data` 的公開 API。
 */
private class FakePagingSource(
    private val ioDispatcher: CoroutineDispatcher,
    private val fetchPage: suspend (page: Int) -> MovieResponse,
) : PagingSource<Int, Movie>() {
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return when (val result = withContext(ioDispatcher) { safeCall { fetchPage(page) } }) {
            is Result.Success -> LoadResult.Page(
                data = result.data.results.map { it.asExternalModel() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= result.data.totalPages) null else page + 1,
            )

            is Result.Error -> LoadResult.Error(NetworkException(result.error))
        }
    }
}
