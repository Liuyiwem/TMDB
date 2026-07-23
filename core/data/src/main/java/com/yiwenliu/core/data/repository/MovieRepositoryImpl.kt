package com.yiwenliu.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yiwenliu.core.common.data.networking.safeCall
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.domain.util.map
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MoviePage
import com.yiwenliu.core.network.api.TMDBApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class MovieRepositoryImpl
@Inject
constructor(
    private val apiService: TMDBApiService,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : MovieRepository {
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { MoviePagingSource(apiService, category, ioDispatcher) },
    ).flow

    override suspend fun searchMovies(
        query: String,
        page: Int,
    ): Result<MoviePage, NetworkError> = withContext(ioDispatcher) {
        safeCall {
            apiService.searchMovies(query, page)
        }.map { it.asExternalModel() }
    }
}
