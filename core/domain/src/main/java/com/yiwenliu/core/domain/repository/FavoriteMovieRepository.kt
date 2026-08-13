package com.yiwenliu.core.domain.repository

import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.EmptyResult
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow

interface FavoriteMovieRepository {
    fun getFavoriteMovies(): Flow<List<FavoriteMovie>>

    fun isFavorite(movieId: Int): Flow<Boolean>

    suspend fun addFavorite(movie: FavoriteMovie): EmptyResult<DataError.Local>

    suspend fun removeFavorite(movieId: Int): EmptyResult<DataError.Local>
}
