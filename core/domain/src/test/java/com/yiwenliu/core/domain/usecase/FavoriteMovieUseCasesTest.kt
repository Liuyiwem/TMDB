package com.yiwenliu.core.domain.usecase

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.testing.data.favoriteMoviesTestData
import com.yiwenliu.core.testing.repository.TestFavoriteMovieRepository
import com.yiwenliu.core.testing.util.firstData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class FavoriteMovieUseCasesTest {
    private val favoriteMovieRepository = TestFavoriteMovieRepository()

    @Test
    fun `GetFavoriteMovies returns what the repository holds`() = runTest {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        assertEquals(favoriteMoviesTestData, GetFavoriteMoviesUseCase(favoriteMovieRepository)().firstData())
    }

    @Test
    fun `SetMovieFavorite with true adds the movie`() = runTest {
        val movie = favoriteMoviesTestData.first()
        SetMovieFavoriteUseCase(favoriteMovieRepository)(movie, true)
        assertEquals(listOf(movie), favoriteMovieRepository.attemptedAdds)
        assertEquals(listOf(movie), favoriteMovieRepository.getFavoriteMovies().firstData())
    }

    @Test
    fun `SetMovieFavorite with false removes the movie`() = runTest {
        val movie = favoriteMoviesTestData.first()
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        SetMovieFavoriteUseCase(favoriteMovieRepository)(movie, false)
        assertEquals(listOf(movie.id), favoriteMovieRepository.attemptedRemovals)
        assertFalse(favoriteMovieRepository.getFavoriteMovies().firstData().contains(movie))
    }

    @Test
    fun `SetMovieFavorite surfaces a write failure`() = runTest {
        favoriteMovieRepository.sendWriteError(DataError.Local.DISK_FULL)
        val result = SetMovieFavoriteUseCase(favoriteMovieRepository)(favoriteMoviesTestData.first(), true)
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }
}
