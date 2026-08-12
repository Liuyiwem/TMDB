package com.yiwenliu.core.data.testdoubles

import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.mock.MockTmdbApiService
import com.yiwenliu.core.network.model.CreditsResponse
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json

@OptIn(ExperimentalCoroutinesApi::class)
class TestTmdbApiService : TmdbApiService {
    private val source =
        MockTmdbApiService(
            ioDispatcher = UnconfinedTestDispatcher(),
            networkJson =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            },
        )

    var errorToThrow: Throwable? = null

    override suspend fun getMoviesByCategory(category: String, page: Int): MovieResponse {
        errorToThrow?.let { throw it }
        return source.getMoviesByCategory(category, page)
    }

    override suspend fun searchMovies(queryString: String, page: Int): MovieResponse {
        errorToThrow?.let { throw it }
        return source.searchMovies(queryString, page)
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetailResponse {
        errorToThrow?.let { throw it }
        return source.getMovieDetail(movieId)
    }

    override suspend fun getMovieCredits(movieId: Int): CreditsResponse {
        errorToThrow?.let { throw it }
        return source.getMovieCredits(movieId)
    }

    override suspend fun getMovieRecommendations(movieId: Int, page: Int): MovieResponse {
        errorToThrow?.let { throw it }
        return source.getMovieRecommendations(movieId, page)
    }
}
