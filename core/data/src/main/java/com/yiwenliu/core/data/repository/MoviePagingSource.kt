package com.yiwenliu.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.common.data.networking.safeApiCall
import com.yiwenliu.core.common.domain.util.DataErrorException
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.network.model.MovieResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
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
        return when (val result = withContext(ioDispatcher) { safeApiCall { fetchPage(page) } }) {
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

            is Result.Failure -> LoadResult.Error(DataErrorException(result.error))
        }
    }
}

fun moviePagingFlow(
    ioDispatcher: CoroutineDispatcher,
    fetchPage: suspend (page: Int) -> MovieResponse,
): Flow<PagingData<Movie>> = Pager(
    config = MOVIE_PAGING_CONFIG,
    pagingSourceFactory = { MoviePagingSource(ioDispatcher, fetchPage) },
).flow

private val MOVIE_PAGING_CONFIG = PagingConfig(pageSize = 20, enablePlaceholders = false)
