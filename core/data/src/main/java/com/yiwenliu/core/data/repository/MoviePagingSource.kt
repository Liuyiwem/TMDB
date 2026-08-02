package com.yiwenliu.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.data.networking.safeCall
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class MoviePagingSource(
    private val ioDispatcher: CoroutineDispatcher,
    private val fetchPage: suspend (page: Int) -> MovieResponse,
) : PagingSource<Int, Movie>() {
    private val seenIds = mutableSetOf<Int>()

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return when (val result = withContext(ioDispatcher) { safeCall { fetchPage(page) } }) {
            is Result.Success -> {
                val movies =
                    result.data.results
                        .map { it.asExternalModel() }
                        .filter { seenIds.add(it.id) }
                LoadResult.Page(
                    data = movies,
                    prevKey = prevPageKeyOf(page),
                    nextKey = nextPageKeyOf(page, result.data.totalPages),
                )
            }

            is Result.Error -> LoadResult.Error(NetworkException(result.error))
        }
    }
}
