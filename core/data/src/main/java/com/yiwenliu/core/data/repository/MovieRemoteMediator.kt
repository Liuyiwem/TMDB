package com.yiwenliu.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.yiwenliu.core.common.di.TimeProvider
import com.yiwenliu.core.common.result.DataErrorException
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.data.model.asEntity
import com.yiwenliu.core.data.util.safeApiCall
import com.yiwenliu.core.data.util.safeDatabaseCall
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
    private val onCacheUpdated: () -> Unit,
) : RemoteMediator<Int, MovieEntity>() {
    override suspend fun initialize(): InitializeAction {
        val remoteKey = safeDatabaseCall { movieDao.remoteKey(category.apiPath) }
        val lastUpdated = when (remoteKey) {
            is Result.Failure -> return InitializeAction.LAUNCH_INITIAL_REFRESH

            is Result.Success ->
                remoteKey.data?.lastUpdated ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        }
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

            LoadType.APPEND -> {
                val remoteKey = safeDatabaseCall { movieDao.remoteKey(category.apiPath) }
                when (remoteKey) {
                    is Result.Failure -> return MediatorResult.Error(DataErrorException(remoteKey.error))

                    is Result.Success ->
                        remoteKey.data?.nextPage
                            ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
        }

        val response = withContext(ioDispatcher) {
            safeApiCall { apiService.getMoviesByCategory(category.apiPath, page) }
        }

        return when (response) {
            is Result.Failure -> MediatorResult.Error(DataErrorException(response.error))

            is Result.Success -> {
                val nextPage = nextPageKeyOf(page, response.data.totalPages)
                val clearExisting = loadType == LoadType.REFRESH
                val saved = safeDatabaseCall {
                    movieDao.saveCategoryPage(
                        category = category.apiPath,
                        movies = response.data.results.map(MovieResult::asEntity),
                        remoteKey = MovieRemoteKeyEntity(
                            category = category.apiPath,
                            nextPage = nextPage,
                            lastUpdated = timeProvider.now(),
                        ),
                        clearExisting = clearExisting,
                    )
                }
                when (saved) {
                    is Result.Failure -> MediatorResult.Error(DataErrorException(saved.error))

                    is Result.Success -> {
                        if (clearExisting) onCacheUpdated()
                        MediatorResult.Success(endOfPaginationReached = nextPage == null)
                    }
                }
            }
        }
    }

    private companion object {
        const val FIRST_PAGE = 1

        val CACHE_TIMEOUT_MILLIS = 30.minutes.inWholeMilliseconds
    }
}
