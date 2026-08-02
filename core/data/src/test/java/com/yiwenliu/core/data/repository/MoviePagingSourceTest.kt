package com.yiwenliu.core.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import androidx.paging.testing.TestPager
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.data.testdoubles.TestTMDBApiService
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.network.model.MovieResponse
import com.yiwenliu.core.network.model.MovieResult
import com.yiwenliu.core.testing.data.moviesTestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MoviePagingSourceTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var apiService: TestTMDBApiService

    @Before
    fun setup() {
        apiService = TestTMDBApiService()
    }

    /** 分類與搜尋共用同一個 [MoviePagingSource]，差別只有餵進去的 fetcher。 */
    private fun categorySource() = MoviePagingSource(testDispatcher) { page ->
        apiService.getMoviesByCategory(MovieCategory.POPULAR.path, page)
    }

    private fun searchSource() = MoviePagingSource(testDispatcher) { page ->
        apiService.searchMovies("fight", page)
    }

    private fun pagerOf(source: MoviePagingSource) = TestPager(PagingConfig(pageSize = 20), source)

    @Test
    fun `load firstPage returns Movies With Correct PagingKeys`() = runTest(testDispatcher) {
        val result = pagerOf(categorySource()).refresh() as LoadResult.Page

        assertEquals(2, result.data.size)
        assertEquals(533535, result.data.first().id)
        assertNull(result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `search fetcher loads the search asset`() = runTest(testDispatcher) {
        // 同一個 load()、不同的 fetcher。這個測試證明合併後的參數化沒有接錯。
        val result = pagerOf(searchSource()).refresh() as LoadResult.Page

        assertEquals(1, result.data.size)
        assertEquals(550, result.data.first().id)
    }

    @Test
    fun `load singlePage endsPagination`() = runTest(testDispatcher) {
        val pager = pagerOf(categorySource())
        pager.refresh()

        assertNull(pager.append())
    }

    @Test
    fun `load networkError returns LoadResultError`() = runTest(testDispatcher) {
        // 兩個 fetcher 共用同一個 load()，所以錯誤路徑只需要驗一次。
        apiService.errorToThrow = NetworkException(NetworkError.NO_INTERNET)

        val result = pagerOf(categorySource()).refresh()

        assertTrue(result is LoadResult.Error)
        assertTrue(result.throwable is NetworkException)
    }

    @Test
    fun `getRefreshKey returns CorrectKey`() = runTest(testDispatcher) {
        val page =
            LoadResult.Page(
                data = moviesTestData,
                prevKey = 1,
                nextKey = 3,
            )
        val pagingState =
            PagingState(
                pages = listOf(page),
                anchorPosition = 0,
                config = PagingConfig(pageSize = 20),
                leadingPlaceholderCount = 0,
            )

        assertEquals(2, categorySource().getRefreshKey(pagingState))
    }

    @Test
    fun `nextKey advances while more pages remain`() = runTest(testDispatcher) {
        // 所有 JSON fixture 的 total_pages 都是 1，所以多頁行為要用本地假資料驗。
        val source = MoviePagingSource(testDispatcher) { page ->
            responseOf(page = page, totalPages = 3, ids = listOf(page * 100))
        }
        val pager = pagerOf(source)

        val first = pager.refresh() as LoadResult.Page
        assertNull(first.prevKey)
        assertEquals(2, first.nextKey)

        val second = pager.append() as LoadResult.Page
        assertEquals(1, second.prevKey)
        assertEquals(3, second.nextKey)

        val third = pager.append() as LoadResult.Page
        assertNull(third.nextKey)
    }

    @Test
    fun `nextKey stops at TMDB's page ceiling even when totalPages is larger`() = runTest(testDispatcher) {
        // TMDB 回報未裁切的 total_pages（movie/popular 約 42,000）卻拒絕 page > 500。
        val source = MoviePagingSource(testDispatcher) { page ->
            responseOf(page = page, totalPages = 42_000, ids = listOf(page))
        }

        val atCeiling = pagerOf(source).refresh(initialKey = TMDB_MAX_PAGE) as LoadResult.Page

        assertNull(atCeiling.nextKey)
    }

    @Test
    fun `duplicate ids across pages are filtered out`() = runTest(testDispatcher) {
        // 這個過濾是在保護 UI：LazyVerticalGrid 用 itemKey { it.id }，Compose 的 lazy layout
        // 碰到重複 key 會拋例外。TMDB 會在請求之間重新排序，所以跨頁重複是真的會發生。
        // 如果有人覺得那行 filter 是多餘的而刪掉，這個測試會擋下來。
        val source = MoviePagingSource(testDispatcher) { page ->
            responseOf(page = page, totalPages = 2, ids = listOf(550, 551))
        }
        val pager = pagerOf(source)

        val first = pager.refresh() as LoadResult.Page
        val second = pager.append() as LoadResult.Page

        assertEquals(listOf(550, 551), first.data.map { it.id })
        // 第二頁回了同樣兩個 id，全部被濾掉；key 仍然前進，所以分頁不會卡住。
        assertTrue(second.data.isEmpty())
        assertNull(second.nextKey)
    }

    private fun responseOf(
        page: Int,
        totalPages: Int,
        ids: List<Int>,
    ) = MovieResponse(
        page = page,
        results = ids.map { MovieResult(id = it, title = "Movie $it") },
        totalPages = totalPages,
        totalResults = totalPages * ids.size,
    )
}
