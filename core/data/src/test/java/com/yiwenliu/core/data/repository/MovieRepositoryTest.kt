package com.yiwenliu.core.data.repository

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.data.testdoubles.TestTMDBApiService
import com.yiwenliu.core.model.MovieCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var apiService: TestTMDBApiService

    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setup() {
        apiService = TestTMDBApiService()
        repository = MovieRepositoryImpl(apiService, testDispatcher)
    }

    @Test
    fun `getMoviesByCategoryPager firstLoad returns CorrectMovies`() =
        runTest(testDispatcher) {
            val movies = repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()

            assertEquals(2, movies.size)
            assertEquals(533535, movies[0].id)
            assertEquals("Deadpool & Wolverine", movies[0].title)
        }
}
