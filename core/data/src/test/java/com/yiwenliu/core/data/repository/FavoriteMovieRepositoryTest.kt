package com.yiwenliu.core.data.repository

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.yiwenliu.core.common.di.TimeProvider
import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.data.testdoubles.TestFavoriteMovieDao
import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FavoriteMovieRepositoryTest {
    private lateinit var favoriteMovieDao: TestFavoriteMovieDao

    private lateinit var repository: FavoriteMovieRepositoryImpl

    private val movie = FavoriteMovie(
        id = 533535,
        title = "Deadpool & Wolverine",
        posterUrl = "/poster.jpg".asImageUrl(),
    )

    private val otherMovie = FavoriteMovie(
        id = 1022789,
        title = "Inside Out 2",
        posterUrl = "/poster2.jpg".asImageUrl(),
    )

    private var currentTime = 0L

    private val timeProvider = TimeProvider { currentTime }

    private suspend fun <T> Flow<Result<T, DataError.Local>>.awaitData(): T = assertIs<Result.Success<T>>(first()).data

    @Before
    fun setup() {
        favoriteMovieDao = TestFavoriteMovieDao()
        repository = FavoriteMovieRepositoryImpl(favoriteMovieDao, timeProvider)
    }

    @Test
    fun `an added movie comes back unchanged`() = runTest {
        repository.addFavorite(movie)
        assertEquals(listOf(movie), repository.getFavoriteMovies().awaitData())
    }

    @Test
    fun `addFavorite returns Success`() = runTest {
        assertIs<Result.Success<Unit>>(repository.addFavorite(movie))
    }

    @Test
    fun `adding two movies returns both`() = runTest {
        repository.addFavorite(movie)
        repository.addFavorite(otherMovie)
        val favorites = repository.getFavoriteMovies().awaitData()
        assertEquals(2, favorites.size)
        assertTrue(favorites.containsAll(listOf(movie, otherMovie)))
    }

    @Test
    fun `adding the same movie twice does not duplicate it`() = runTest {
        repository.addFavorite(movie)
        repository.addFavorite(movie)
        assertEquals(listOf(movie), repository.getFavoriteMovies().awaitData())
    }

    @Test
    fun `isFavorite reflects what is stored`() = runTest {
        assertFalse(repository.isFavorite(movie.id).first())
        repository.addFavorite(movie)
        assertTrue(repository.isFavorite(movie.id).first())
        assertFalse(repository.isFavorite(otherMovie.id).first())
    }

    @Test
    fun `removeFavorite removes only that movie`() = runTest {
        repository.addFavorite(movie)
        repository.addFavorite(otherMovie)
        assertIs<Result.Success<Unit>>(repository.removeFavorite(movie.id))
        assertEquals(listOf(otherMovie), repository.getFavoriteMovies().awaitData())
    }

    @Test
    fun `addFavorite surfaces a full disk as a Result Failure`() = runTest {
        favoriteMovieDao.errorToThrow = SQLiteFullException("disk full")
        val result = repository.addFavorite(movie)
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `removeFavorite surfaces an unknown database failure as a Result Failure`() = runTest {
        favoriteMovieDao.errorToThrow = SQLiteException("boom")
        val result = repository.removeFavorite(movie.id)
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `favorites come back newest first`() = runTest {
        currentTime = 100L
        repository.addFavorite(movie)
        currentTime = 200L
        repository.addFavorite(otherMovie)
        assertEquals(listOf(otherMovie, movie), repository.getFavoriteMovies().awaitData())
    }

    @Test
    fun `getFavoriteMovies surfaces a read failure as a Result Failure`() = runTest {
        repository.addFavorite(movie)
        favoriteMovieDao.readErrorToThrow = SQLiteException("boom")
        val result = repository.getFavoriteMovies().first()
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `getFavoriteMovies surfaces a full disk as a Result Failure`() = runTest {
        repository.addFavorite(movie)
        favoriteMovieDao.readErrorToThrow = SQLiteFullException("disk full")
        val result = repository.getFavoriteMovies().first()
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `isFavorite degrades to false when the read fails`() = runTest {
        repository.addFavorite(movie)
        favoriteMovieDao.readErrorToThrow = SQLiteException("boom")
        assertFalse(repository.isFavorite(movie.id).first())
    }
}
