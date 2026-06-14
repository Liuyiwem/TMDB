package com.yiwenliu.core.data.model

import com.yiwenliu.core.network.model.MovieResult
import org.junit.Test
import kotlin.test.assertEquals
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
        assertTrue(movie.posterPath.endsWith("w500/poster.jpg"))
    }
}
