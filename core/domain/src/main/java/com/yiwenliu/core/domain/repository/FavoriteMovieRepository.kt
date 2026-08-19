package com.yiwenliu.core.domain.repository

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.EmptyResult
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow

interface FavoriteMovieRepository {
    fun getFavoriteMovies(): Flow<Result<List<FavoriteMovie>, DataError.Local>>

    fun isFavorite(movieId: Int): Flow<Boolean>

    suspend fun addFavorite(movie: FavoriteMovie): EmptyResult<DataError.Local>

    suspend fun removeFavorite(movieId: Int): EmptyResult<DataError.Local>
}
