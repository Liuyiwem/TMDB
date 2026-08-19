package com.yiwenliu.core.domain.usecase

import app.cash.turbine.test
import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.model.MovieDetailBundle
import com.yiwenliu.core.testing.data.castTestData
import com.yiwenliu.core.testing.data.favoriteMoviesTestData
import com.yiwenliu.core.testing.data.movieDetailTestData
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestFavoriteMovieRepository
import com.yiwenliu.core.testing.repository.TestMovieRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetMovieDetailUseCaseTest {
    private val movieRepository = TestMovieRepository()

    private val favoriteMovieRepository = TestFavoriteMovieRepository()

    private lateinit var useCase: GetMovieDetailUseCase

    @Before
    fun setup() {
        useCase = GetMovieDetailUseCase(movieRepository, favoriteMovieRepository)
        movieRepository.sendMovieDetail(movieDetailTestData)
        movieRepository.sendCast(castTestData)
        movieRepository.sendRecommendations(moviesTestData)
    }

    @Test
    fun `invoke bundles detail cast and recommendations`() = runTest {
        val result = useCase(533535).first()
        assertIs<Result.Success<MovieDetailBundle>>(result)
        assertEquals(movieDetailTestData, result.data.detail)
        assertEquals(castTestData, result.data.cast)
        assertEquals(moviesTestData, result.data.recommendations)
    }

    @Test
    fun `invoke passes the same movieId to all three calls`() = runTest {
        useCase(533535).first()
        assertEquals(listOf(533535), movieRepository.requestedDetailIds)
        assertEquals(listOf(533535), movieRepository.requestedCreditsIds)
        assertEquals(listOf(533535), movieRepository.requestedRecommendationIds)
    }

    @Test
    fun `invoke runs the three requests in parallel`() = runTest {
        movieRepository.responseDelayMillis = 1_000
        val start = currentTime
        useCase(533535).first()
        assertEquals(1_000L, currentTime - start)
    }

    @Test
    fun `a movie that is not stored is not favorite`() = runTest {
        val result = useCase(533535).first()
        assertIs<Result.Success<MovieDetailBundle>>(result)
        assertFalse(result.data.isFavorite)
    }

    @Test
    fun `a stored movie is reported as favorite`() = runTest {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)

        val result = useCase(533535).first()
        assertIs<Result.Success<MovieDetailBundle>>(result)
        assertTrue(result.data.isFavorite)
    }

    @Test
    fun `favoriting re-emits the bundle without refetching`() = runTest {
        useCase(533535).test {
            assertFalse(assertIs<Result.Success<MovieDetailBundle>>(awaitItem()).data.isFavorite)

            favoriteMovieRepository.addFavorite(favoriteMoviesTestData.first())
            assertTrue(assertIs<Result.Success<MovieDetailBundle>>(awaitItem()).data.isFavorite)
            assertEquals(listOf(533535), movieRepository.requestedDetailIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a detail failure fails the whole bundle`() = runTest {
        movieRepository.sendDetailError(DataError.Remote.NO_INTERNET)
        val result = useCase(533535).first()
        assertIs<Result.Failure<DataError.Remote>>(result)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }

    @Test
    fun `a credits failure fails the whole bundle`() = runTest {
        movieRepository.sendCreditsError(DataError.Remote.SERVER_ERROR)
        val result = useCase(533535).first()
        assertIs<Result.Failure<DataError.Remote>>(result)
        assertEquals(DataError.Remote.SERVER_ERROR, result.error)
    }

    @Test
    fun `a recommendations failure fails the whole bundle`() = runTest {
        movieRepository.sendRecommendationsError(DataError.Remote.SERVER_ERROR)
        val result = useCase(533535).first()
        assertIs<Result.Failure<DataError.Remote>>(result)
        assertEquals(DataError.Remote.SERVER_ERROR, result.error)
    }

    @Test
    fun `a detail error wins over a recommendations error`() = runTest {
        movieRepository.sendDetailError(DataError.Remote.NO_INTERNET)
        movieRepository.sendRecommendationsError(DataError.Remote.SERVER_ERROR)
        val result = useCase(533535).first()
        assertIs<Result.Failure<DataError.Remote>>(result)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }
}
