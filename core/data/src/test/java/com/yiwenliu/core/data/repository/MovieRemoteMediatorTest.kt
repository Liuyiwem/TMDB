package com.yiwenliu.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.InitializeAction
import androidx.paging.RemoteMediator.MediatorResult
import com.yiwenliu.core.common.di.TimeProvider
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.DataErrorException
import com.yiwenliu.core.data.testdoubles.TestMovieDao
import com.yiwenliu.core.database.model.MovieCategoryIndexEntity
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.database.model.MovieRemoteKeyEntity
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.model.CreditsResponse
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import com.yiwenliu.core.network.model.MovieResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeCategoryApiService : TmdbApiService {
    var errorToThrow: Throwable? = null

    var requestedPages = mutableListOf<Int>()

    var fetchPage: (Int) -> MovieResponse = { page -> responseOf(page, totalPages = 1, ids = listOf(1, 2)) }

    override suspend fun getMoviesByCategory(category: String, page: Int): MovieResponse {
        requestedPages += page
        errorToThrow?.let { throw it }
        return fetchPage(page)
    }

    override suspend fun searchMovies(queryString: String, page: Int): MovieResponse = throw NotImplementedError()

    override suspend fun getMovieDetail(movieId: Int): MovieDetailResponse = throw NotImplementedError()

    override suspend fun getMovieCredits(movieId: Int): CreditsResponse = throw NotImplementedError()

    override suspend fun getMovieRecommendations(movieId: Int, page: Int): MovieResponse = throw NotImplementedError()
}

private fun responseOf(page: Int, totalPages: Int, ids: List<Int>) = MovieResponse(
    page = page,
    results = ids.map { MovieResult(id = it, title = "Movie $it") },
    totalPages = totalPages,
    totalResults = totalPages * ids.size,
)

@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
class MovieRemoteMediatorTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val apiService = FakeCategoryApiService()

    private val movieDao = TestMovieDao()

    private var currentTime = 1_000_000L

    private val category = MovieCategory.POPULAR

    private var cacheUpdates = 0

    private val mediator = MovieRemoteMediator(
        category = category,
        apiService = apiService,
        movieDao = movieDao,
        timeProvider = TimeProvider { currentTime },
        ioDispatcher = testDispatcher,
        onCacheUpdated = { cacheUpdates++ },
    )

    private fun pagingState() = PagingState<Int, MovieEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    @Test
    fun `refresh stores the movies the index and the remote key`() = runTest(testDispatcher) {
        apiService.fetchPage = { page -> responseOf(page, totalPages = 3, ids = listOf(10, 11)) }

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertIs<MediatorResult.Success>(result)
        assertFalse(result.endOfPaginationReached)
        assertEquals(listOf(10, 11), movieDao.moviesIn(category.path).map(MovieEntity::id))
        assertEquals(listOf(0, 1), movieDao.indexOf(category.path).map(MovieCategoryIndexEntity::position))
        assertEquals(2, movieDao.remoteKey(category.path)?.nextPage)
        assertEquals(currentTime, movieDao.remoteKey(category.path)?.lastUpdated)
    }

    @Test
    fun `refresh clears the previous index and renumbers positions from zero`() = runTest(testDispatcher) {
        apiService.fetchPage = { page -> responseOf(page, totalPages = 3, ids = listOf(10, 11)) }
        mediator.load(LoadType.REFRESH, pagingState())
        mediator.load(LoadType.APPEND, pagingState())

        apiService.fetchPage = { page -> responseOf(page, totalPages = 3, ids = listOf(20, 21)) }
        mediator.load(LoadType.REFRESH, pagingState())

        assertEquals(listOf(20, 21), movieDao.moviesIn(category.path).map(MovieEntity::id))
        assertEquals(listOf(0, 1), movieDao.indexOf(category.path).map(MovieCategoryIndexEntity::position))
    }

    @Test
    fun `append continues positions from the stored maximum regardless of page size`() = runTest(testDispatcher) {
        apiService.fetchPage = { page ->
            when (page) {
                1 -> responseOf(page, totalPages = 3, ids = listOf(1, 2, 3))
                else -> responseOf(page, totalPages = 3, ids = listOf(4, 5))
            }
        }

        mediator.load(LoadType.REFRESH, pagingState())
        val appended = mediator.load(LoadType.APPEND, pagingState())

        assertIs<MediatorResult.Success>(appended)
        assertEquals(listOf(1, 2, 3, 4, 5), movieDao.moviesIn(category.path).map(MovieEntity::id))
        assertEquals(
            listOf(0, 1, 2, 3, 4),
            movieDao.indexOf(category.path).map(MovieCategoryIndexEntity::position),
        )
        assertEquals(listOf(1, 2), apiService.requestedPages)
    }

    @Test
    fun `append without a next page ends pagination without calling the api`() = runTest(testDispatcher) {
        movieDao.seedRemoteKey(
            MovieRemoteKeyEntity(category = category.path, nextPage = null, lastUpdated = currentTime),
        )

        val result = mediator.load(LoadType.APPEND, pagingState())

        assertIs<MediatorResult.Success>(result)
        assertTrue(result.endOfPaginationReached)
        assertTrue(apiService.requestedPages.isEmpty())
    }

    @Test
    fun `a single page refresh reaches the end of pagination`() = runTest(testDispatcher) {
        apiService.fetchPage = { page -> responseOf(page, totalPages = 1, ids = listOf(10, 11)) }

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertIs<MediatorResult.Success>(result)
        assertTrue(result.endOfPaginationReached)
        assertNull(movieDao.remoteKey(category.path)?.nextPage)
    }

    @Test
    fun `prepend ends pagination without calling the api`() = runTest(testDispatcher) {
        val result = mediator.load(LoadType.PREPEND, pagingState())

        assertIs<MediatorResult.Success>(result)
        assertTrue(result.endOfPaginationReached)
        assertTrue(apiService.requestedPages.isEmpty())
    }

    @Test
    fun `a network failure maps to a mediator error carrying the remote error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")

        val result = mediator.load(LoadType.REFRESH, pagingState())

        assertIs<MediatorResult.Error>(result)
        assertEquals(DataError.Remote.NO_INTERNET, (result.throwable as DataErrorException).error)
        assertTrue(movieDao.storedMovies.isEmpty())
    }

    @Test
    fun `a refresh keeps movies that another category still references`() = runTest(testDispatcher) {
        apiService.fetchPage = { page -> responseOf(page, totalPages = 3, ids = listOf(10, 11)) }
        mediator.load(LoadType.REFRESH, pagingState())
        movieDao.insertCategoryIndex(
            listOf(MovieCategoryIndexEntity(MovieCategory.NOW_PLAYING.path, movieId = 11, position = 0)),
        )

        apiService.fetchPage = { page -> responseOf(page, totalPages = 3, ids = listOf(20)) }
        mediator.load(LoadType.REFRESH, pagingState())

        assertEquals(listOf(11, 20), movieDao.storedMovies.map(MovieEntity::id).sorted())
        assertEquals(listOf(11), movieDao.moviesIn(MovieCategory.NOW_PLAYING.path).map(MovieEntity::id))
    }

    @Test
    fun `only a refresh reports that the cache changed`() = runTest(testDispatcher) {
        apiService.fetchPage = { page -> responseOf(page, totalPages = 3, ids = listOf(10, 11)) }

        mediator.load(LoadType.REFRESH, pagingState())
        assertEquals(1, cacheUpdates)

        mediator.load(LoadType.APPEND, pagingState())
        assertEquals(1, cacheUpdates)
    }

    @Test
    fun `a failed load does not report a cache change`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")

        mediator.load(LoadType.REFRESH, pagingState())

        assertEquals(0, cacheUpdates)
    }

    @Test
    fun `ending pagination without fetching does not report a cache change`() = runTest(testDispatcher) {
        movieDao.seedRemoteKey(
            MovieRemoteKeyEntity(category = category.path, nextPage = null, lastUpdated = currentTime),
        )

        mediator.load(LoadType.APPEND, pagingState())

        assertEquals(0, cacheUpdates)
    }

    @Test
    fun `initialize launches a refresh without a remote key`() = runTest(testDispatcher) {
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `initialize skips a refresh within the cache timeout`() = runTest(testDispatcher) {
        movieDao.seedRemoteKey(
            MovieRemoteKeyEntity(category.path, nextPage = 2, lastUpdated = currentTime - 29 * 60 * 1000L),
        )

        assertEquals(InitializeAction.SKIP_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `initialize launches a refresh after the cache timeout`() = runTest(testDispatcher) {
        movieDao.seedRemoteKey(
            MovieRemoteKeyEntity(category.path, nextPage = 2, lastUpdated = currentTime - 31 * 60 * 1000L),
        )

        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `initialize reads only the remote key of its own category`() = runTest(testDispatcher) {
        movieDao.seedRemoteKey(
            MovieRemoteKeyEntity(MovieCategory.TOP_RATED.path, nextPage = 2, lastUpdated = currentTime),
        )

        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }
}
