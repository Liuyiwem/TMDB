package com.yiwenliu.core.testing.repository

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.EmptyResult
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.domain.repository.FavoriteMovieRepository
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestFavoriteMovieRepository : FavoriteMovieRepository {
    private val favorites = MutableStateFlow<List<FavoriteMovie>>(emptyList())

    private var writeError: DataError.Local? = null

    private val readError = MutableStateFlow<DataError.Local?>(null)

    val attemptedAdds = mutableListOf<FavoriteMovie>()

    val attemptedRemovals = mutableListOf<Int>()

    override fun getFavoriteMovies(): Flow<Result<List<FavoriteMovie>, DataError.Local>> =
        combine(favorites, readError) { movies, error ->
            error?.let { Result.Failure(it) } ?: Result.Success(movies)
        }

    override fun isFavorite(movieId: Int): Flow<Boolean> = favorites.map { movies ->
        movies.any { it.id == movieId }
    }

    override suspend fun addFavorite(movie: FavoriteMovie): EmptyResult<DataError.Local> {
        attemptedAdds += movie
        writeError?.let { return Result.Failure(it) }
        favorites.update { current -> listOf(movie) + current.filterNot { it.id == movie.id } }
        return Result.Success(Unit)
    }

    override suspend fun removeFavorite(movieId: Int): EmptyResult<DataError.Local> {
        attemptedRemovals += movieId
        writeError?.let { return Result.Failure(it) }
        favorites.update { current -> current.filterNot { it.id == movieId } }
        return Result.Success(Unit)
    }

    fun sendFavoriteMovies(movies: List<FavoriteMovie>) {
        favorites.value = movies
    }

    fun sendWriteError(error: DataError.Local?) {
        writeError = error
    }

    fun sendReadError(error: DataError.Local?) {
        readError.value = error
    }
}
