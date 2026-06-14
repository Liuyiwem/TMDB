package com.yiwenliu.core.network.mock

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MockTMDBApiServiceTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var apiService: MockTMDBApiService

    @Before
    fun setup() {
        val networkJson =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            }
        apiService = MockTMDBApiService(testDispatcher, networkJson)
    }

    @Test
    fun `getPopularMovies loads popular_movies asset`() =
        runTest(testDispatcher) {
            val response = apiService.getPopularMovies()

            assertEquals(2, response.results.size)
            assertEquals(533535, response.results[0].id)
            assertEquals("Deadpool & Wolverine", response.results[0].title)
        }

    @Test
    fun `getTopRatedMovies loads top_rated_movies asset`() =
        runTest(testDispatcher) {
            val response = apiService.getTopRatedMovies()

            assertTrue(response.results.isNotEmpty())
        }

    @Test
    fun `getUpcomingMovies loads upcoming_movies asset`() =
        runTest(testDispatcher) {
            val response = apiService.getUpcomingMovies()

            assertTrue(response.results.isNotEmpty())
        }

    @Test
    fun `searchMovies loads search_movies asset`() =
        runTest(testDispatcher) {
            val response = apiService.searchMovies(query = "fight")

            assertTrue(response.results.isNotEmpty())
        }
}
