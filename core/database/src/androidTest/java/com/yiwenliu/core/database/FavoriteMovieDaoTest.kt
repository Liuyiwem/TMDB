package com.yiwenliu.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yiwenliu.core.database.dao.FavoriteMovieDao
import com.yiwenliu.core.database.model.FavoriteMovieEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class FavoriteMovieDaoTest {
    private lateinit var database: TmdbDatabase
    private lateinit var favoriteMovieDao: FavoriteMovieDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TmdbDatabase::class.java,
        ).build()
        favoriteMovieDao = database.favoriteMovieDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertedMovieIsReturnedAsFavorite() = runTest {
        favoriteMovieDao.upsert(entity(id = 1, createdAt = 100L))

        assertTrue(favoriteMovieDao.isFavorite(1).first())
        assertFalse(favoriteMovieDao.isFavorite(2).first())
    }

    @Test
    fun favoritesAreOrderedByNewestFirst() = runTest {
        favoriteMovieDao.upsert(entity(id = 1, title = "Oldest", createdAt = 100L))
        favoriteMovieDao.upsert(entity(id = 2, title = "Newest", createdAt = 300L))
        favoriteMovieDao.upsert(entity(id = 3, title = "Middle", createdAt = 200L))

        val titles = favoriteMovieDao.getFavoriteMovies().first().map { it.title }

        assertEquals(listOf("Newest", "Middle", "Oldest"), titles)
    }

    @Test
    fun upsertReplacesTheExistingRow() = runTest {
        favoriteMovieDao.upsert(entity(id = 1, title = "Before", createdAt = 100L))
        favoriteMovieDao.upsert(entity(id = 1, title = "After", createdAt = 200L))

        val favorites = favoriteMovieDao.getFavoriteMovies().first()

        assertEquals(1, favorites.size)
        assertEquals("After", favorites.first().title)
    }

    @Test
    fun deleteRemovesTheMovie() = runTest {
        favoriteMovieDao.upsert(entity(id = 1, createdAt = 100L))
        favoriteMovieDao.delete(1)

        assertTrue(favoriteMovieDao.getFavoriteMovies().first().isEmpty())
        assertFalse(favoriteMovieDao.isFavorite(1).first())
    }

    private fun entity(id: Int, title: String = "Movie $id", createdAt: Long) = FavoriteMovieEntity(
        id = id,
        title = title,
        posterPath = "https://image.tmdb.org/t/p/w500/poster$id.jpg",
        createdAt = createdAt,
    )
}
