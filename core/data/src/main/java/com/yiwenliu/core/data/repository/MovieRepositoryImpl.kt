package com.yiwenliu.core.data.repository

import androidx.paging.PagingData
import com.yiwenliu.core.common.data.networking.safeApiCall
import com.yiwenliu.core.common.di.Dispatcher
import com.yiwenliu.core.common.di.TmdbDispatchers.IO
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.domain.util.map
import com.yiwenliu.core.data.model.asExternalModel
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class MovieRepositoryImpl
@Inject
constructor(
    private val apiService: TmdbApiService,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : MovieRepository {
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> =
        moviePagingFlow(ioDispatcher) { page ->
            apiService.getMoviesByCategory(category.path, page)
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
