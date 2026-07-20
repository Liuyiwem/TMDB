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
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MoviePage
import kotlinx.coroutines.flow.Flow

class TestMovieRepository : MovieRepository {
    private var movies: List<Movie> = emptyList()

    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> =
        Pager(PagingConfig(pageSize = 20)) {
            object : PagingSource<Int, Movie>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> =
                    LoadResult.Page(data = movies, prevKey = null, nextKey = null)

                override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null
            }
        }.flow

    fun sendMovies(movies: List<Movie>) {
        this.movies = movies
    }

    private val emptyResponse =
        MoviePage(page = 1, movies = emptyList(), totalPages = 1, totalResults = 0)

    var searchResult: Result<MoviePage, NetworkError> = Result.Success(emptyResponse)

    override suspend fun searchMovies(
        query: String,
        page: Int,
    ) = searchResult
}
