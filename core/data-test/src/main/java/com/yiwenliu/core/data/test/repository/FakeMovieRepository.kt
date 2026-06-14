package com.yiwenliu.core.data.test.repository

import androidx.paging.PagingData
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MoviePage
import com.yiwenliu.core.network.mock.MockTMDBApiService
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FakeMovieRepository
    @Inject
    constructor(
        @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
        private val datasource: MockTMDBApiService,
    ) : MovieRepository {
        override fun getPopularMoviesPager(): Flow<PagingData<Movie>> =
            flow {
                val response = datasource.getPopularMovies()
                val movies = response.results.map { it.asExternalModel() }
                emit(PagingData.from(movies))
            }.flowOn(ioDispatcher)

        override suspend fun getPopularMovies(page: Int) = success(datasource.getPopularMovies(page))

        override suspend fun getTopRatedMovies(page: Int) = success(datasource.getTopRatedMovies(page))

        override suspend fun getUpcomingMovies(page: Int) = success(datasource.getUpcomingMovies(page))

        override suspend fun searchMovies(
            query: String,
            page: Int,
        ) = success(datasource.searchMovies(query, page))

        private suspend fun success(call: MovieResponse): Result<MoviePage, NetworkError> =
            withContext(ioDispatcher) { Result.Success(call.asExternalModel()) }
    }
