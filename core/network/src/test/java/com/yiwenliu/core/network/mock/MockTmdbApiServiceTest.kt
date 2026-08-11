package com.yiwenliu.core.network.mock

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun `getMoviesByCategory popular loads popular_movies asset`() = runTest(testDispatcher) {
        val response = apiService.getMoviesByCategory("popular")
        assertEquals(2, response.results.size)
        assertEquals(533535, response.results[0].id)
        assertEquals("Deadpool & Wolverine", response.results[0].title)
    }

    @Test
    fun `getMoviesByCategory now_playing loads now_playing_movies asset`() = runTest(testDispatcher) {
        val response = apiService.getMoviesByCategory("now_playing")
        assertEquals(2, response.results.size)
        assertEquals(1241982, response.results[0].id)
        assertEquals("Moana 2", response.results[0].title)
    }

    @Test
    fun `getMoviesByCategory top_rated loads top_rated_movies asset`() = runTest(testDispatcher) {
        assertTrue(apiService.getMoviesByCategory("top_rated").results.isNotEmpty())
    }

    @Test
    fun `getMoviesByCategory upcoming loads upcoming_movies asset`() = runTest(testDispatcher) {
        assertTrue(apiService.getMoviesByCategory("upcoming").results.isNotEmpty())
    }

    @Test
    fun `searchMovies loads search_movies asset`() = runTest(testDispatcher) {
        assertTrue(apiService.searchMovies(queryString = "fight").results.isNotEmpty())
    }

    @Test
    fun `searchMovies loads the empty asset for the reserved query`() = runTest(testDispatcher) {
        val response = apiService.searchMovies(MockTMDBApiService.EMPTY_RESULT_QUERY)
        assertTrue(response.results.isEmpty())
        assertEquals(0, response.totalResults)
    }

    @Test
    fun `getMovieDetail loads movie_detail asset`() = runTest(testDispatcher) {
        val response = apiService.getMovieDetail(533535)
        assertEquals(533535, response.id)
        assertEquals("Deadpool & Wolverine", response.title)
        assertEquals("Come together.", response.tagline)
        assertEquals(128, response.runtime)
        assertEquals(3, response.genres.size)
        assertEquals("Action", response.genres[0].name)
    }

    @Test
    fun `getMovieCredits loads movie_credits asset`() = runTest(testDispatcher) {
        val response = apiService.getMovieCredits(533535)
        assertEquals(3, response.cast.size)
        assertEquals("Ryan Reynolds", response.cast[0].name)
        assertEquals("Wade Wilson / Deadpool", response.cast[0].character)
    }

    @Test
    fun `getMovieCredits coerces a null profile path to an empty string`() = runTest(testDispatcher) {
        val emmaCorrin = apiService.getMovieCredits(533535).cast.first { it.id == 1667888 }
        assertEquals("", emmaCorrin.profilePath)
    }

    @Test
    fun `getMovieRecommendations loads movie_recommendations asset`() = runTest(testDispatcher) {
        val response = apiService.getMovieRecommendations(533535)
        assertEquals(2, response.results.size)
        assertEquals(1022789, response.results[0].id)
        assertEquals("Inside Out 2", response.results[0].title)
        assertEquals(1, response.totalPages)
    }

    @Test
    fun `errorToThrow is honoured by every endpoint`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("no internet")
        assertFailsWith<IOException> { apiService.getMoviesByCategory("popular") }
        assertFailsWith<IOException> { apiService.searchMovies("fight") }
        assertFailsWith<IOException> { apiService.getMovieDetail(533535) }
        assertFailsWith<IOException> { apiService.getMovieCredits(533535) }
        assertFailsWith<IOException> { apiService.getMovieRecommendations(533535) }
    }
}
