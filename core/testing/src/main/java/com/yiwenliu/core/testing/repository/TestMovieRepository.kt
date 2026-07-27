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

    /**
     * 實際被請求的分類，依請求順序記錄。
     *
     * 記錄發生在 [PagingSource.load] 內，所以斷言之前必須先觸發一次載入
     * （例如 `asSnapshot()`），否則清單會是空的。
     */
    val requestedCategories = mutableListOf<MovieCategory>()

    /** 實際被請求的查詢字串，依請求順序記錄。與 [requestedCategories] 同樣需要先觸發載入。 */
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

    /** 讓後續的載入回傳 [LoadResult.Error]。傳 `null` 可恢復正常載入。 */
    fun sendError(error: Throwable?) {
        this.loadError = error
    }
}
