package com.yiwenliu.core.database

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yiwenliu.core.database.dao.MovieDao
import com.yiwenliu.core.database.model.MovieCategoryIndexEntity
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.database.model.MovieRemoteKeyEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val POPULAR = "popular"

private const val NOW_PLAYING = "now_playing"

@RunWith(AndroidJUnit4::class)
class MovieDaoTest {
    private lateinit var database: TmdbDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TmdbDatabase::class.java,
        ).build()
        movieDao = database.movieDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun pagingSourceReturnsTheCategoryInPositionOrder() = runTest {
        savePage(POPULAR, ids = listOf(30, 10, 20), clearExisting = true)
        assertEquals(listOf(30, 10, 20), loadedIds(POPULAR))
    }

    @Test
    fun pagingSourceExcludesOtherCategories() = runTest {
        savePage(POPULAR, ids = listOf(1, 2), clearExisting = true)
        savePage(NOW_PLAYING, ids = listOf(3, 4), clearExisting = true)
        assertEquals(listOf(1, 2), loadedIds(POPULAR))
        assertEquals(listOf(3, 4), loadedIds(NOW_PLAYING))
    }

    @Test
    fun appendContinuesPositionsAcrossPages() = runTest {
        savePage(POPULAR, ids = listOf(1, 2, 3), clearExisting = true)
        savePage(POPULAR, ids = listOf(4, 5), clearExisting = false)
        assertEquals(listOf(1, 2, 3, 4, 5), loadedIds(POPULAR))
        assertEquals(4, movieDao.maxPosition(POPULAR))
    }

    @Test
    fun aMovieSharedByTwoCategoriesIsStoredOnce() = runTest {
        savePage(POPULAR, ids = listOf(1, 2), clearExisting = true)
        savePage(NOW_PLAYING, ids = listOf(2, 3), clearExisting = true)
        assertEquals(listOf(1, 2, 3), allMovieIds())
        assertEquals(listOf(2, 3), loadedIds(NOW_PLAYING))
    }

    @Test
    fun refreshClearsTheCategoryButKeepsMoviesReferencedElsewhere() = runTest {
        savePage(POPULAR, ids = listOf(1, 2), clearExisting = true)
        savePage(NOW_PLAYING, ids = listOf(2), clearExisting = true)
        savePage(POPULAR, ids = listOf(9), clearExisting = true)
        assertEquals(listOf(2, 9), allMovieIds())
        assertEquals(listOf(9), loadedIds(POPULAR))
        assertEquals(listOf(2), loadedIds(NOW_PLAYING))
    }

    @Test
    fun refreshRenumbersPositionsFromZero() = runTest {
        savePage(POPULAR, ids = listOf(1, 2), clearExisting = true)
        savePage(POPULAR, ids = listOf(3, 4), clearExisting = false)
        savePage(POPULAR, ids = listOf(5, 6), clearExisting = true)
        assertEquals(1, movieDao.maxPosition(POPULAR))
        assertEquals(listOf(5, 6), loadedIds(POPULAR))
    }

    @Test
    fun duplicateIndexRowsKeepTheOriginalPosition() = runTest {
        savePage(POPULAR, ids = listOf(1, 2), clearExisting = true)
        savePage(POPULAR, ids = listOf(2, 3), clearExisting = false)
        assertEquals(listOf(1, 2, 3), loadedIds(POPULAR))
    }

    @Test
    fun maxPositionIsMinusOneForAnUnknownCategory() = runTest {
        assertEquals(-1, movieDao.maxPosition(POPULAR))
    }

    @Test
    fun remoteKeyRoundTripsAndIsScopedToItsCategory() = runTest {
        movieDao.upsertRemoteKey(MovieRemoteKeyEntity(POPULAR, nextPage = 3, lastUpdated = 42L))
        val stored = movieDao.remoteKey(POPULAR)
        assertEquals(3, stored?.nextPage)
        assertEquals(42L, stored?.lastUpdated)
        assertNull(movieDao.remoteKey(NOW_PLAYING))
    }

    @Test
    fun aNullNextPageRoundTrips() = runTest {
        movieDao.upsertRemoteKey(MovieRemoteKeyEntity(POPULAR, nextPage = null, lastUpdated = 1L))
        assertNull(movieDao.remoteKey(POPULAR)?.nextPage)
    }

    @Test
    fun genreIdsRoundTripThroughTheTypeConverter() = runTest {
        movieDao.upsertMovies(listOf(movieEntity(id = 1, genreIds = listOf(28, 12, 878))))
        movieDao.insertCategoryIndex(listOf(MovieCategoryIndexEntity(POPULAR, movieId = 1, position = 0)))
        assertEquals(listOf(28, 12, 878), loadedMovies(POPULAR).single().genreIds)
    }

    @Test
    fun emptyGenreIdsRoundTrip() = runTest {
        movieDao.upsertMovies(listOf(movieEntity(id = 1, genreIds = emptyList())))
        movieDao.insertCategoryIndex(listOf(MovieCategoryIndexEntity(POPULAR, movieId = 1, position = 0)))
        assertEquals(emptyList<Int>(), loadedMovies(POPULAR).single().genreIds)
    }

    private suspend fun savePage(category: String, ids: List<Int>, clearExisting: Boolean) = movieDao.saveCategoryPage(
        category = category,
        movies = ids.map { movieEntity(id = it) },
        remoteKey = MovieRemoteKeyEntity(category, nextPage = 2, lastUpdated = 0L),
        clearExisting = clearExisting,
    )

    private suspend fun loadedMovies(category: String): List<MovieEntity> {
        val result = movieDao.pagingSource(category).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false),
        )
        return (result as LoadResult.Page).data
    }

    private suspend fun loadedIds(category: String) = loadedMovies(category).map(MovieEntity::id)

    private fun allMovieIds(): List<Int> =
        database.openHelper.readableDatabase.query("SELECT id FROM movies ORDER BY id").use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getInt(0)) }
        }

    private fun movieEntity(id: Int, genreIds: List<Int> = listOf(id)) = MovieEntity(
        id = id,
        title = "Movie $id",
        overview = "Overview $id",
        releaseDate = "2024-01-0$id",
        posterPath = "/poster$id.jpg",
        backdropPath = "/backdrop$id.jpg",
        voteAverage = 7.5,
        voteCount = 100 + id,
        popularity = 50.0 + id,
        genreIds = genreIds,
    )
}
