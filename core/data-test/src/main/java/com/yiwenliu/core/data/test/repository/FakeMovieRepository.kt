package com.yiwenliu.core.data.test.repository

import androidx.paging.PagingData
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MoviePage
import com.yiwenliu.core.network.mock.MockTMDBApiService
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
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = flow {
        val response = datasource.getMoviesByCategory(category.path)
        val movies = response.results.map { it.asExternalModel() }
        emit(PagingData.from(movies))
    }.flowOn(ioDispatcher)

    override suspend fun searchMovies(
        query: String,
        page: Int,
    ): Result<MoviePage, NetworkError> = withContext(ioDispatcher) { Result.Success(datasource.searchMovies(query, page).asExternalModel()) }
}
