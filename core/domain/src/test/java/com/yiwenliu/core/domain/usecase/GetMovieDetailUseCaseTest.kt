package com.yiwenliu.core.domain.usecase

import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.model.MovieDetailBundle
import com.yiwenliu.core.testing.data.castTestData
import com.yiwenliu.core.testing.data.movieDetailTestData
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetMovieDetailUseCaseTest {
    private val movieRepository = TestMovieRepository()

    private lateinit var useCase: GetMovieDetailUseCase

    @Before
    fun setup() {
        useCase = GetMovieDetailUseCase(movieRepository)
        movieRepository.sendMovieDetail(movieDetailTestData)
        movieRepository.sendCast(castTestData)
        movieRepository.sendRecommendations(moviesTestData)
    }

    @Test
    fun `invoke bundles detail cast and recommendations`() = runTest {
        val result = useCase(533535)
        assertIs<Result.Success<MovieDetailBundle>>(result)
        assertEquals(movieDetailTestData, result.data.detail)
        assertEquals(castTestData, result.data.cast)
        assertEquals(moviesTestData, result.data.recommendations)
    }

    @Test
    fun `invoke passes the same movieId to all three calls`() = runTest {
        useCase(533535)
        assertEquals(listOf(533535), movieRepository.requestedDetailIds)
        assertEquals(listOf(533535), movieRepository.requestedCreditsIds)
        assertEquals(listOf(533535), movieRepository.requestedRecommendationIds)
    }

    @Test
    fun `invoke runs the three requests in parallel`() = runTest {
        movieRepository.responseDelayMillis = 1_000
        val start = currentTime
        useCase(533535)
        assertEquals(1_000L, currentTime - start)
    }

    @Test
    fun `a detail failure fails the whole bundle`() = runTest {
        movieRepository.sendDetailError(NetworkError.NO_INTERNET)
        val result = useCase(533535)
        assertIs<Result.Error<NetworkError>>(result)
        assertEquals(NetworkError.NO_INTERNET, result.error)
    }

    @Test
    fun `a credits failure fails the whole bundle`() = runTest {
        movieRepository.sendCreditsError(NetworkError.SERVER_ERROR)
        val result = useCase(533535)
        assertIs<Result.Error<NetworkError>>(result)
        assertEquals(NetworkError.SERVER_ERROR, result.error)
    }

    @Test
    fun `a recommendations failure fails the whole bundle`() = runTest {
        movieRepository.sendRecommendationsError(NetworkError.SERVER_ERROR)
        val result = useCase(533535)
        assertIs<Result.Error<NetworkError>>(result)
        assertEquals(NetworkError.SERVER_ERROR, result.error)
    }

    @Test
    fun `a detail error wins over a recommendations error`() = runTest {
        movieRepository.sendDetailError(NetworkError.NO_INTERNET)
        movieRepository.sendRecommendationsError(NetworkError.SERVER_ERROR)
        val result = useCase(533535)
        assertIs<Result.Error<NetworkError>>(result)
        assertEquals(NetworkError.NO_INTERNET, result.error)
    }
}
