package com.yiwenliu.core.testing.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import kotlinx.coroutines.flow.Flow

class TestMovieRepository : MovieRepository {
    private var movies: List<Movie> = emptyList()

    private var loadError: Throwable? = null

    val requestedCategories = mutableListOf<MovieCategory>()

    val requestedQueries = mutableListOf<String>()

    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = pager {
        requestedCategories += category
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = pager {
        requestedQueries += queryString
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
}
