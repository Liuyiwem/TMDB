package com.yiwenliu.core.data.testdoubles

import com.yiwenliu.core.database.dao.FavoriteMovieDao
import com.yiwenliu.core.database.model.FavoriteMovieEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestFavoriteMovieDao : FavoriteMovieDao {
    private val entitiesStateFlow = MutableStateFlow(emptyList<FavoriteMovieEntity>())

    var errorToThrow: Throwable? = null

    var readErrorToThrow: Throwable? = null

    override fun getFavoriteMovies(): Flow<List<FavoriteMovieEntity>> = entitiesStateFlow.map { entities ->
        readErrorToThrow?.let { throw it }
        entities.sortedByDescending(FavoriteMovieEntity::createdAt)
    }

    override fun isFavorite(movieId: Int): Flow<Boolean> = entitiesStateFlow.map { entities ->
        readErrorToThrow?.let { throw it }
        entities.any { it.id == movieId }
    }

    override suspend fun upsert(favoriteMovie: FavoriteMovieEntity) {
        errorToThrow?.let { throw it }
        entitiesStateFlow.update { current ->
            listOf(favoriteMovie) + current.filterNot { it.id == favoriteMovie.id }
        }
    }

    override suspend fun delete(movieId: Int) {
        errorToThrow?.let { throw it }
        entitiesStateFlow.update { current -> current.filterNot { it.id == movieId } }
    }
}
