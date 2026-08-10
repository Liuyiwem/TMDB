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
import kotlinx.serialization.SerializationException
import org.junit.Before
import org.junit.Test
import java.io.IOException
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
    fun `an IOException maps to NO_INTERNET`() = runTest(testDispatcher) {
        val source = MoviePagingSource(testDispatcher) { throw IOException("socket closed") }
        val result = pagerOf(source).refresh()
        assertTrue(result is LoadResult.Error)
        assertEquals(NetworkError.NO_INTERNET, (result.throwable as NetworkException).networkError)
    }

    @Test
    fun `a SerializationException maps to SERIALIZATION`() = runTest(testDispatcher) {
        val source = MoviePagingSource(testDispatcher) {
            throw SerializationException("unexpected JSON token")
        }
        val result = pagerOf(source).refresh()
        assertEquals(
            NetworkError.SERIALIZATION,
            ((result as LoadResult.Error).throwable as NetworkException).networkError,
        )
    }

    @Test
    fun `an append failure carries the mapped NetworkError`() = runTest(testDispatcher) {
        val source = MoviePagingSource(testDispatcher) { page ->
            if (page == 1) responseOf(page = 1, totalPages = 3, ids = listOf(1, 2)) else throw IOException("boom")
        }
        val pager = pagerOf(source)
        pager.refresh()
        val appended = pager.append()
        assertEquals(
            NetworkError.NO_INTERNET,
            ((appended as LoadResult.Error).throwable as NetworkException).networkError,
        )
    }

    @Test
    fun `retrying after an error still returns the page`() = runTest(testDispatcher) {
        var attempts = 0
        val source = MoviePagingSource(testDispatcher) { page ->
            if (attempts++ == 0) throw IOException("boom") else responseOf(page, totalPages = 1, ids = listOf(550, 551))
        }
        assertTrue(pagerOf(source).refresh() is LoadResult.Error)
        val retried = pagerOf(source).refresh() as LoadResult.Page
        assertEquals(listOf(550, 551), retried.data.map { it.id })
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
        val source = MoviePagingSource(testDispatcher) { page ->
            responseOf(page = page, totalPages = 42_000, ids = listOf(page))
        }
        val atCeiling = pagerOf(source).refresh(initialKey = TMDB_MAX_PAGE) as LoadResult.Page
        assertNull(atCeiling.nextKey)
    }

    @Test
    fun `duplicate ids across pages are filtered out`() = runTest(testDispatcher) {
        val source = MoviePagingSource(testDispatcher) { page ->
            responseOf(page = page, totalPages = 2, ids = listOf(550, 551))
        }
        val pager = pagerOf(source)
        val first = pager.refresh() as LoadResult.Page
        val second = pager.append() as LoadResult.Page
        assertEquals(listOf(550, 551), first.data.map { it.id })
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
