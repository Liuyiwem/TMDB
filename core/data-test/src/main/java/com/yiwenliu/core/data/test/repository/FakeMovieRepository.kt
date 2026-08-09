package com.yiwenliu.core.data.test.repository

import androidx.paging.PagingData
import com.yiwenliu.core.common.data.networking.safeCall
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.domain.util.map
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.data.repository.moviePagingFlow
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import com.yiwenliu.core.network.mock.MockTMDBApiService
import com.yiwenliu.core.network.model.CreditsResponse
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FakeMovieRepository
@Inject
constructor(
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val datasource: MockTMDBApiService,
) : MovieRepository {
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = moviePagingFlow(ioDispatcher) { page ->
        datasource.getMoviesByCategory(category.path, page)
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = moviePagingFlow(ioDispatcher) { page ->
        datasource.searchMovies(queryString, page)
    }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, NetworkError> = withContext(ioDispatcher) {
        safeCall { datasource.getMovieDetail(movieId) }.map(MovieDetailResponse::asExternalModel)
    }

    override suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, NetworkError> = withContext(ioDispatcher) {
        safeCall { datasource.getMovieCredits(movieId) }.map(CreditsResponse::asExternalModel)
    }

    override suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, NetworkError> = withContext(ioDispatcher) {
        safeCall { datasource.getMovieRecommendations(movieId) }.map(MovieResponse::asExternalModel)
    }
}
