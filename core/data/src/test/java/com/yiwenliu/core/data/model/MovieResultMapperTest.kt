package com.yiwenliu.core.data.model

import com.yiwenliu.core.network.model.MovieResponse
import com.yiwenliu.core.network.model.MovieResult
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovieResultMapperTest {
    @Test
    fun `asExternalModel maps fields and keeps voteAverage numeric`() {
        val dto =
            MovieResult(
                id = 533535,
                title = "Deadpool & Wolverine",
                overview = "overview",
                releaseDate = "2024-07-24",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
                voteAverage = 7.7,
                voteCount = 100,
                popularity = 50.0,
                genreIds = listOf(28, 35),
            )
        val movie = dto.asExternalModel()
        assertEquals(533535, movie.id)
        assertEquals("Deadpool & Wolverine", movie.title)
        assertEquals(7.7, movie.voteAverage, 0.0)
        assertEquals(100, movie.voteCount)
        assertEquals(listOf(28, 35), movie.genreIds)
        assertTrue(assertNotNull(movie.posterUrl).endsWith("/poster.jpg"))
    }

    @Test
    fun `asExternalModel builds poster and backdrop urls from their own paths`() {
        val movie =
            MovieResult(
                id = 533535,
                title = "Deadpool & Wolverine",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
            ).asExternalModel()
        assertTrue(assertNotNull(movie.posterUrl).endsWith("/poster.jpg"))
        assertTrue(assertNotNull(movie.backdropUrl).endsWith("/backdrop.jpg"))
    }

    @Test
    fun `asExternalModel maps a blank image path to null`() {
        val movie = MovieResult(id = 533535, posterPath = "", backdropPath = "").asExternalModel()
        assertNull(movie.posterUrl)
        assertNull(movie.backdropUrl)
    }

    @Test
    fun `MovieResponse asExternalModel maps every result`() {
        val movies =
            MovieResponse(
                page = 1,
                results =
                listOf(
                    MovieResult(id = 533535, title = "Deadpool & Wolverine"),
                    MovieResult(id = 1022789, title = "Inside Out 2"),
                ),
                totalPages = 1,
                totalResults = 2,
            ).asExternalModel()
        assertEquals(listOf(533535, 1022789), movies.map { it.id })
    }
}
