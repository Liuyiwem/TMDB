package com.yiwenliu.core.data.test.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.data.networking.safeCall
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.domain.util.map
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import com.yiwenliu.core.network.mock.MockTMDBApiService
import com.yiwenliu.core.network.model.CastResult
import com.yiwenliu.core.network.model.MovieDetailResponse
import com.yiwenliu.core.network.model.MovieResponse
import com.yiwenliu.core.network.model.MovieResult
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
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = pager { page ->
        datasource.getMoviesByCategory(category.path, page)
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = pager { page ->
        datasource.searchMovies(queryString, page)
    }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, NetworkError> = withContext(ioDispatcher) {
        safeCall { datasource.getMovieDetail(movieId) }.map(MovieDetailResponse::asExternalModel)
    }

    override suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, NetworkError> = withContext(ioDispatcher) {
        safeCall { datasource.getMovieCredits(movieId) }.map { response ->
            response.cast.sortedBy(CastResult::order).map(CastResult::asExternalModel)
        }
    }

    override suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, NetworkError> = withContext(ioDispatcher) {
        safeCall { datasource.getMovieRecommendations(movieId) }.map { response ->
            response.results.map(MovieResult::asExternalModel)
        }
    }

    private fun pager(fetchPage: suspend (page: Int) -> MovieResponse): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { FakePagingSource(ioDispatcher, fetchPage) },
    ).flow
}

private class FakePagingSource(
    private val ioDispatcher: CoroutineDispatcher,
    private val fetchPage: suspend (page: Int) -> MovieResponse,
) : PagingSource<Int, Movie>() {
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return when (val result = withContext(ioDispatcher) { safeCall { fetchPage(page) } }) {
            is Result.Success -> LoadResult.Page(
                data = result.data.results.map { it.asExternalModel() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= result.data.totalPages) null else page + 1,
            )

            is Result.Error -> LoadResult.Error(NetworkException(result.error))
        }
    }
}
