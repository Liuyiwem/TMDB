package com.yiwenliu.core.testing.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.domain.repository.MovieRepository
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import kotlinx.coroutines.delay
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

    private var detailError: DataError.Remote? = null

    private var creditsError: DataError.Remote? = null

    private var recommendationsError: DataError.Remote? = null

    var responseDelayMillis: Long = 0

    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = pager {
        requestedCategories += category
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = pager {
        requestedQueries += queryString
    }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, DataError.Remote> {
        delayIfNeeded()
        requestedDetailIds += movieId
        return detailError?.let { Result.Failure(it) } ?: Result.Success(checkNotNull(movieDetail))
    }

    override suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, DataError.Remote> {
        delayIfNeeded()
        requestedCreditsIds += movieId
        return creditsError?.let { Result.Failure(it) } ?: Result.Success(cast)
    }

    override suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, DataError.Remote> {
        delayIfNeeded()
        requestedRecommendationIds += movieId
        return recommendationsError?.let { Result.Failure(it) } ?: Result.Success(recommendations)
    }

    private suspend fun delayIfNeeded() {
        if (responseDelayMillis > 0) delay(responseDelayMillis)
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

    fun sendDetailError(error: DataError.Remote?) {
        this.detailError = error
    }

    fun sendCreditsError(error: DataError.Remote?) {
        this.creditsError = error
    }

    fun sendRecommendationsError(error: DataError.Remote?) {
        this.recommendationsError = error
    }
}
