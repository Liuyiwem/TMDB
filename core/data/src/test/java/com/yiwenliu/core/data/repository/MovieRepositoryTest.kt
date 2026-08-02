package com.yiwenliu.core.data.repository

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.data.testdoubles.TestTMDBApiService
import com.yiwenliu.core.model.MovieCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var apiService: TestTMDBApiService

    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setup() {
        apiService = TestTMDBApiService()
        repository = MovieRepositoryImpl(apiService, testDispatcher)
    }

    @Test
    fun `getMoviesByCategoryPager firstLoad returns CorrectMovies`() = runTest(testDispatcher) {
        val movies = repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()
        assertEquals(2, movies.size)
        assertEquals(533535, movies[0].id)
        assertEquals("Deadpool & Wolverine", movies[0].title)
    }

    @Test
    fun `searchMoviesPager firstLoad returns CorrectMovies`() = runTest(testDispatcher) {
        val movies = repository.searchMoviesPager("fight").asSnapshot()
        assertEquals(1, movies.size)
        assertEquals(550, movies[0].id)
        assertEquals("Fight Club", movies[0].title)
    }

    @Test
    fun `getMoviesByCategoryPager surfaces a failure as a load error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")
        val thrown = assertFailsWith<NetworkException> {
            repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()
        }
        assertEquals(NetworkError.NO_INTERNET, thrown.networkError)
    }

    @Test
    fun `searchMoviesPager surfaces a failure as a load error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")
        val thrown = assertFailsWith<NetworkException> {
            repository.searchMoviesPager("batman").asSnapshot()
        }
        assertEquals(NetworkError.NO_INTERNET, thrown.networkError)
    }
}
