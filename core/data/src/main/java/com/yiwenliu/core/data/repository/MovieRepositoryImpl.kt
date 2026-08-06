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
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import com.yiwenliu.core.network.api.TMDBApiService
import com.yiwenliu.core.network.model.CastResult
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import com.yiwenliu.core.network.model.MovieResult
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
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = moviePager { page ->
        apiService.getMoviesByCategory(category.path, page)
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = moviePager { page ->
        apiService.searchMovies(queryString, page)
    }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, NetworkError> = withContext(ioDispatcher) {
        safeCall { apiService.getMovieDetail(movieId) }.map(MovieDetailResponse::asExternalModel)
    }

    override suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, NetworkError> = withContext(ioDispatcher) {
        safeCall { apiService.getMovieCredits(movieId) }.map { response ->
            response.cast.sortedBy(CastResult::order).map(CastResult::asExternalModel)
        }
    }

    override suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, NetworkError> = withContext(ioDispatcher) {
        safeCall { apiService.getMovieRecommendations(movieId) }.map { response ->
            response.results.map(MovieResult::asExternalModel)
        }
    }

    private fun moviePager(fetchPage: suspend (page: Int) -> MovieResponse): Flow<PagingData<Movie>> = Pager(
        config = MOVIE_PAGING_CONFIG,
        pagingSourceFactory = { MoviePagingSource(ioDispatcher, fetchPage) },
    ).flow
}

private val MOVIE_PAGING_CONFIG = PagingConfig(pageSize = 20, enablePlaceholders = false)
