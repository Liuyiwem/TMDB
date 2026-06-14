package com.yiwenliu.core.testing.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MoviePage
import kotlinx.coroutines.flow.Flow

class TestMovieRepository : MovieRepository {
    private var popularMovies: List<Movie> = emptyList()

    override fun getPopularMoviesPager(): Flow<PagingData<Movie>> =
        Pager(PagingConfig(pageSize = 20)) {
            object : PagingSource<Int, Movie>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> =
                    LoadResult.Page(data = popularMovies, prevKey = null, nextKey = null)

                override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null
            }
        }.flow

    fun sendPopularMovies(movies: List<Movie>) {
        popularMovies = movies
    }

    private val emptyResponse =
        MoviePage(page = 1, movies = emptyList(), totalPages = 1, totalResults = 0)

    var popularMoviesResult: Result<MoviePage, NetworkError> = Result.Success(emptyResponse)

    override suspend fun getPopularMovies(page: Int) = popularMoviesResult

    override suspend fun getTopRatedMovies(page: Int) = popularMoviesResult

    override suspend fun getUpcomingMovies(page: Int) = popularMoviesResult

    override suspend fun searchMovies(
        query: String,
        page: Int,
    ) = popularMoviesResult
}
