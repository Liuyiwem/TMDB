package com.yiwenliu.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yiwenliu.core.common.network.Dispatcher
import com.yiwenliu.core.common.network.TMDBDispatchers.IO
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.network.api.TMDBApiService
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class MovieRepositoryImpl
@Inject
constructor(
    private val apiService: TMDBApiService,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : MovieRepository {
    override fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>> = moviePager { page ->
        apiService.getMoviesByCategory(category.path, page)
    }

    override fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>> = moviePager { page ->
        apiService.searchMovies(queryString, page)
    }

    private fun moviePager(fetchPage: suspend (page: Int) -> MovieResponse): Flow<PagingData<Movie>> = Pager(
        config = MOVIE_PAGING_CONFIG,
        pagingSourceFactory = { MoviePagingSource(ioDispatcher, fetchPage) },
    ).flow
}

/**
 * 分類與搜尋共用同一份設定。
 *
 * 刻意不設 `maxSize`——原因見 [MoviePagingSource.seenIds] 的說明。
 */
private val MOVIE_PAGING_CONFIG = PagingConfig(pageSize = 20, enablePlaceholders = false)
