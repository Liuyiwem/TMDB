package com.yiwenliu.core.data.test.repository

import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.EmptyResult
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.repository.FavoriteMovieRepository
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal class FakeFavoriteMovieRepository
@Inject
constructor() : FavoriteMovieRepository {
    private val favorites = MutableStateFlow<List<FavoriteMovie>>(emptyList())

    override fun getFavoriteMovies(): Flow<List<FavoriteMovie>> = favorites.asStateFlow()

    override fun isFavorite(movieId: Int): Flow<Boolean> = favorites.map { movies -> movies.any { it.id == movieId } }

    override suspend fun addFavorite(movie: FavoriteMovie): EmptyResult<DataError.Local> {
        favorites.update { current -> listOf(movie) + current.filterNot { it.id == movie.id } }
        return Result.Success(Unit)
    }

    override suspend fun removeFavorite(movieId: Int): EmptyResult<DataError.Local> {
        favorites.update { current -> current.filterNot { it.id == movieId } }
        return Result.Success(Unit)
    }
}
