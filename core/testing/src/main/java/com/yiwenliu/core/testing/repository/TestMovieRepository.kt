package com.yiwenliu.core.testing.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import kotlinx.coroutines.flow.Flow

class TestMovieRepository : MovieRepository {
    private var movies: List<Movie> = emptyList()

    private var loadError: Throwable? = null

    val requestedCategories = mutableListOf<MovieCategory>()

    val requestedQueries = mutableListOf<String>()

    val requestedDetailIds = mutableListOf<Int>()

    val requestedCreditsIds = mutableListOf<Int>()

    val requestedRecommendationIds = mutableListOf<Int>()

    private var movieDetail: MovieDetail? = null

    private var cast: List<CastMember> = emptyList()

    private var recommendations: List<Movie> = emptyList()

    private var detailError: NetworkError? = null

    private var creditsError: NetworkError? = null

    private var recommendationsError: NetworkError? = null

    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = pager {
        requestedCategories += category
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = pager {
        requestedQueries += queryString
    }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, NetworkError> {
        requestedDetailIds += movieId
        return detailError?.let { Result.Error(it) } ?: Result.Success(checkNotNull(movieDetail))
    }

    override suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, NetworkError> {
        requestedCreditsIds += movieId
        return creditsError?.let { Result.Error(it) } ?: Result.Success(cast)
    }

    override suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, NetworkError> {
        requestedRecommendationIds += movieId
        return recommendationsError?.let { Result.Error(it) } ?: Result.Success(recommendations)
    }

    private fun pager(onLoad: () -> Unit): Flow<PagingData<Movie>> = Pager(
        PagingConfig(pageSize = 20, enablePlaceholders = false),
    ) {
        object : PagingSource<Int, Movie>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
                onLoad()
                loadError?.let { return LoadResult.Error(it) }
                return LoadResult.Page(data = movies, prevKey = null, nextKey = null)
            }

            override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null
        }
    }.flow

    fun sendMovies(movies: List<Movie>) {
        this.movies = movies
    }

    fun sendError(error: Throwable?) {
        this.loadError = error
    }

    fun sendMovieDetail(movieDetail: MovieDetail?) {
        this.movieDetail = movieDetail
    }

    fun sendCast(cast: List<CastMember>) {
        this.cast = cast
    }

    fun sendRecommendations(recommendations: List<Movie>) {
        this.recommendations = recommendations
    }

    fun sendDetailError(error: NetworkError?) {
        this.detailError = error
    }

    fun sendCreditsError(error: NetworkError?) {
        this.creditsError = error
    }

    fun sendRecommendationsError(error: NetworkError?) {
        this.recommendationsError = error
    }
}
