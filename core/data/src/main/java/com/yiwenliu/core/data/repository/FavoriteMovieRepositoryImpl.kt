package com.yiwenliu.core.data.repository

import com.yiwenliu.core.common.di.TimeProvider
import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.EmptyResult
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.common.result.asEmptyResult
import com.yiwenliu.core.data.model.asEntity
import com.yiwenliu.core.data.model.asExternalModel
import com.yiwenliu.core.data.util.asDatabaseResult
import com.yiwenliu.core.data.util.safeDatabaseCall
import com.yiwenliu.core.database.dao.FavoriteMovieDao
import com.yiwenliu.core.database.model.FavoriteMovieEntity
import com.yiwenliu.core.domain.repository.FavoriteMovieRepository
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class FavoriteMovieRepositoryImpl
@Inject
constructor(
    private val favoriteMovieDao: FavoriteMovieDao,
    private val timeProvider: TimeProvider,
) : FavoriteMovieRepository {
    override fun getFavoriteMovies(): Flow<Result<List<FavoriteMovie>, DataError.Local>> =
        favoriteMovieDao.getFavoriteMovies()
            .map { entities -> entities.map(FavoriteMovieEntity::asExternalModel) }
            .asDatabaseResult()

    override fun isFavorite(movieId: Int): Flow<Boolean> = favoriteMovieDao.isFavorite(movieId)
        .catch { emit(false) }

    override suspend fun addFavorite(movie: FavoriteMovie): EmptyResult<DataError.Local> = safeDatabaseCall {
        favoriteMovieDao.upsert(movie.asEntity(createdAt = timeProvider.now()))
    }.asEmptyResult()

    override suspend fun removeFavorite(movieId: Int): EmptyResult<DataError.Local> = safeDatabaseCall {
        favoriteMovieDao.delete(movieId)
    }.asEmptyResult()
}
