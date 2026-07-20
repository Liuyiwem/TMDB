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
    fun `getMoviesByCategory popular loads popular_movies asset`() =
        runTest(testDispatcher) {
            val response = apiService.getMoviesByCategory("popular")

            assertEquals(2, response.results.size)
            assertEquals(533535, response.results[0].id)
            assertEquals("Deadpool & Wolverine", response.results[0].title)
        }

    @Test
    fun `getMoviesByCategory now_playing loads now_playing_movies asset`() =
        runTest(testDispatcher) {
            val response = apiService.getMoviesByCategory("now_playing")

            assertEquals(2, response.results.size)
            assertEquals(1241982, response.results[0].id)
            assertEquals("Moana 2", response.results[0].title)
        }

    @Test
    fun `getMoviesByCategory top_rated loads top_rated_movies asset`() =
        runTest(testDispatcher) {
            assertTrue(apiService.getMoviesByCategory("top_rated").results.isNotEmpty())
        }

    @Test
    fun `getMoviesByCategory upcoming loads upcoming_movies asset`() =
        runTest(testDispatcher) {
            assertTrue(apiService.getMoviesByCategory("upcoming").results.isNotEmpty())
        }

    @Test
    fun `searchMovies loads search_movies asset`() =
        runTest(testDispatcher) {
            assertTrue(apiService.searchMovies(query = "fight").results.isNotEmpty())
        }
}
