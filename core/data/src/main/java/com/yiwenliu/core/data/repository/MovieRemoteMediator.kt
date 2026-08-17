package com.yiwenliu.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.yiwenliu.core.common.data.networking.safeApiCall
import com.yiwenliu.core.common.di.TimeProvider
import com.yiwenliu.core.common.domain.util.DataErrorException
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.model.asEntity
import com.yiwenliu.core.database.dao.MovieDao
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.database.model.MovieRemoteKeyEntity
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.model.MovieResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalPagingApi::class)
internal class MovieRemoteMediator(
    private val category: MovieCategory,
    private val apiService: TmdbApiService,
    private val movieDao: MovieDao,
    private val timeProvider: TimeProvider,
    private val ioDispatcher: CoroutineDispatcher,
) : RemoteMediator<Int, MovieEntity>() {
    override suspend fun initialize(): InitializeAction {
        val lastUpdated =
            movieDao.remoteKey(category.path)?.lastUpdated ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        return if (timeProvider.now() - lastUpdated <= CACHE_TIMEOUT_MILLIS) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, MovieEntity>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> FIRST_PAGE

            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND ->
                movieDao.remoteKey(category.path)?.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        val response = withContext(ioDispatcher) {
            safeApiCall { apiService.getMoviesByCategory(category.path, page) }
        }

        return when (response) {
            is Result.Failure -> MediatorResult.Error(DataErrorException(response.error))

            is Result.Success -> {
                val nextPage = nextPageKeyOf(page, response.data.totalPages)
                movieDao.saveCategoryPage(
                    category = category.path,
                    movies = response.data.results.map(MovieResult::asEntity),
                    remoteKey = MovieRemoteKeyEntity(
                        category = category.path,
                        nextPage = nextPage,
                        lastUpdated = timeProvider.now(),
                    ),
                    clearExisting = loadType == LoadType.REFRESH,
                )
                MediatorResult.Success(endOfPaginationReached = nextPage == null)
            }
        }
    }

    private companion object {
        const val FIRST_PAGE = 1

        val CACHE_TIMEOUT_MILLIS = 30.minutes.inWholeMilliseconds
    }
}
