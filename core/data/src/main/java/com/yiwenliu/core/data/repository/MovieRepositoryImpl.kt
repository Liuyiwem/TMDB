package com.yiwenliu.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.yiwenliu.core.common.di.Dispatcher
import com.yiwenliu.core.common.di.TimeProvider
import com.yiwenliu.core.common.di.TmdbDispatchers.IO
import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.common.result.map
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.data.util.safeApiCall
import com.yiwenliu.core.database.dao.MovieDao
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.domain.repository.MovieRepository
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.model.CreditsResponse
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class MovieRepositoryImpl
@Inject
constructor(
    private val apiService: TmdbApiService,
    private val movieDao: MovieDao,
    private val timeProvider: TimeProvider,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : MovieRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> {
        val pagingSourceFactory = InvalidatingPagingSourceFactory { movieDao.pagingSource(category.apiPath) }
        return Pager(
            config = MOVIE_PAGING_CONFIG,
            remoteMediator = MovieRemoteMediator(
                category = category,
                apiService = apiService,
                movieDao = movieDao,
                timeProvider = timeProvider,
                ioDispatcher = ioDispatcher,
                onCacheUpdated = pagingSourceFactory::invalidate,
            ),
            pagingSourceFactory = pagingSourceFactory,
        ).flow.map { pagingData -> pagingData.map(MovieEntity::asExternalModel) }
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> =
        moviePagingFlow(ioDispatcher) { page ->
            apiService.searchMovies(queryString, page)
        }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, DataError.Remote> =
        withContext(ioDispatcher) {
            safeApiCall { apiService.getMovieDetail(movieId) }.map(MovieDetailResponse::asExternalModel)
        }

    override suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, DataError.Remote> =
        withContext(ioDispatcher) {
            safeApiCall { apiService.getMovieCredits(movieId) }.map(CreditsResponse::asExternalModel)
        }

    override suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, DataError.Remote> =
        withContext(ioDispatcher) {
            safeApiCall { apiService.getMovieRecommendations(movieId) }.map(MovieResponse::asExternalModel)
        }
}
