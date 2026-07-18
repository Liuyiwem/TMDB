package com.yiwenliu.core.data.testdoubles

import com.yiwenliu.core.network.api.TMDBApiService
import com.yiwenliu.core.network.mock.MockTMDBApiService
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json

@OptIn(ExperimentalCoroutinesApi::class)
class TestTMDBApiService : TMDBApiService {
    private val source =
        MockTMDBApiService(
            ioDispatcher = UnconfinedTestDispatcher(),
            networkJson =
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    isLenient = true
                },
        )

    var errorToThrow: Throwable? = null

    override suspend fun getMoviesByCategory(
        category: String,
        page: Int,
    ): MovieResponse {
        errorToThrow?.let { throw it }
        return source.getMoviesByCategory(category, page)
    }

    override suspend fun searchMovies(
        query: String,
        page: Int,
    ) = source.searchMovies(query, page)
}
