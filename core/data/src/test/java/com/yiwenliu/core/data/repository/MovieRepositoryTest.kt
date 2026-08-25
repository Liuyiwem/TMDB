package com.yiwenliu.core.data.repository

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.common.di.TimeProvider
import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.DataErrorException
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.data.testdoubles.TestMovieDao
import com.yiwenliu.core.data.testdoubles.TestTmdbApiService
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var apiService: TestTmdbApiService

    private lateinit var movieDao: TestMovieDao

    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setup() {
        apiService = TestTmdbApiService()
        movieDao = TestMovieDao()
        repository = MovieRepositoryImpl(apiService, movieDao, TimeProvider { 0L }, testDispatcher)
    }

    @Test
    fun `getMoviesByCategoryPager caches the fetched movies and emits them from the database`() =
        runTest(testDispatcher) {
            val movies = repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()
            assertEquals(2, movies.size)
            assertEquals(533535, movies[0].id)
            assertEquals("Deadpool & Wolverine", movies[0].title)
            assertEquals(
                movies.map(Movie::id),
                movieDao.moviesIn(MovieCategory.POPULAR.apiPath).map(MovieEntity::id),
            )
        }

    @Test
    fun `getMoviesByCategoryPager serves the cache without hitting the network again`() = runTest(testDispatcher) {
        repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()
        apiService.errorToThrow = IOException("boom")
        val cached = repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()
        assertEquals(listOf(533535, 1022789), cached.map(Movie::id))
    }

    @Test
    fun `searchMoviesPager returns the matching movies`() = runTest(testDispatcher) {
        val movies = repository.searchMoviesPager("fight").asSnapshot()
        assertEquals(1, movies.size)
        assertEquals(550, movies[0].id)
        assertEquals("Fight Club", movies[0].title)
    }

    @Test
    fun `getMoviesByCategoryPager surfaces a failure as a load error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")
        val thrown = assertFailsWith<DataErrorException> {
            repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()
        }
        assertEquals(DataError.Remote.NO_INTERNET, thrown.error)
        assertTrue(movieDao.storedMovies.isEmpty())
    }

    @Test
    fun `searchMoviesPager surfaces a failure as a load error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")
        val thrown = assertFailsWith<DataErrorException> {
            repository.searchMoviesPager("batman").asSnapshot()
        }
        assertEquals(DataError.Remote.NO_INTERNET, thrown.error)
    }

    @Test
    fun `getMovieDetail returns the mapped detail`() = runTest(testDispatcher) {
        val result = repository.getMovieDetail(533535)
        assertIs<Result.Success<MovieDetail>>(result)
        assertEquals(533535, result.data.id)
        assertEquals("Deadpool & Wolverine", result.data.title)
        assertEquals(128, result.data.runtimeMinutes)
        assertEquals(3, result.data.genres.size)
    }

    @Test
    fun `getMovieCredits returns the mapped cast`() = runTest(testDispatcher) {
        val result = repository.getMovieCredits(533535)
        assertIs<Result.Success<List<CastMember>>>(result)
        assertEquals(3, result.data.size)
        assertEquals("Ryan Reynolds", result.data[0].name)
    }

    @Test
    fun `getMovieRecommendations returns the mapped movies`() = runTest(testDispatcher) {
        val result = repository.getMovieRecommendations(533535)
        assertIs<Result.Success<List<Movie>>>(result)
        assertEquals(2, result.data.size)
        assertEquals(1022789, result.data[0].id)
    }

    @Test
    fun `getMovieDetail surfaces a failure as a Result Error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")
        val result = repository.getMovieDetail(533535)
        assertIs<Result.Failure<DataError.Remote>>(result)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }

    @Test
    fun `getMovieCredits surfaces a failure as a Result Error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")
        val result = repository.getMovieCredits(533535)
        assertIs<Result.Failure<DataError.Remote>>(result)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }

    @Test
    fun `getMovieRecommendations surfaces a failure as a Result Error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")
        val result = repository.getMovieRecommendations(533535)
        assertIs<Result.Failure<DataError.Remote>>(result)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }
}
