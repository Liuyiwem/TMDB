package com.yiwenliu.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.data.networking.safeCall
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.network.api.TMDBApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class MoviePagingSource(
    private val apiService: TMDBApiService,
    private val category: MovieCategory,
    private val ioDispatcher: CoroutineDispatcher,
) : PagingSource<Int, Movie>() {
    private val seenIds = mutableSetOf<Int>()

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return when (
            val result =
                withContext(ioDispatcher) { safeCall { apiService.getMoviesByCategory(category.path, page) } }
        ) {
            is Result.Success -> {
                val movies =
                    result.data.results
                        .map { it.asExternalModel() }
                        .filter { seenIds.add(it.id) }
                LoadResult.Page(
                    data = movies,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (page >= result.data.totalPages) null else page + 1,
                )
            }

            is Result.Error -> LoadResult.Error(NetworkException(result.error))
        }
    }
}
