package com.yiwenliu.core.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import androidx.paging.testing.TestPager
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.data.testdoubles.TestTMDBApiService
import com.yiwenliu.core.model.MovieCategory
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

    private lateinit var pagingSource: MoviePagingSource

    @Before
    fun setup() {
        apiService = TestTMDBApiService()
        pagingSource = MoviePagingSource(apiService, MovieCategory.POPULAR, testDispatcher)
    }

    @Test
    fun `load firstPage returns Movies With Correct PagingKeys`() = runTest(testDispatcher) {
        val pager = TestPager(PagingConfig(pageSize = 20), pagingSource)

        val result = pager.refresh() as LoadResult.Page

        assertEquals(2, result.data.size)
        assertNull(result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `load singlePage endsPagination`() = runTest(testDispatcher) {
        val pager = TestPager(PagingConfig(pageSize = 20), pagingSource)
        pager.refresh()

        assertNull(pager.append())
    }

    @Test
    fun `load networkError returns LoadResultError`() = runTest(testDispatcher) {
        apiService.errorToThrow = NetworkException(NetworkError.NO_INTERNET)

        val pager = TestPager(PagingConfig(pageSize = 20), pagingSource)
        val result = pager.refresh()

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

        val refreshKey = pagingSource.getRefreshKey(pagingState)
        assertEquals(2, refreshKey)
    }
}
