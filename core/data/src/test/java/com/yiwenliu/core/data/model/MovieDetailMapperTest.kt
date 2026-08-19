package com.yiwenliu.core.data.model

import com.yiwenliu.core.network.model.CastResult
import com.yiwenliu.core.network.model.CreditsResponse
import com.yiwenliu.core.network.model.GenreResult
import com.yiwenliu.core.network.model.MovieDetailResponse
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovieDetailMapperTest {
    private val detailResponse =
        MovieDetailResponse(
            id = 533535,
            title = "Deadpool & Wolverine",
            tagline = "Come together.",
            overview = "overview",
            releaseDate = "2024-07-24",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            runtime = 128,
            status = "Released",
            voteAverage = 7.6,
            voteCount = 5842,
            genres = listOf(GenreResult(28, "Action"), GenreResult(35, "Comedy")),
        )

    @Test
    fun `asExternalModel maps every detail field`() {
        val detail = detailResponse.asExternalModel()
        assertEquals(533535, detail.id)
        assertEquals("Deadpool & Wolverine", detail.title)
        assertEquals("Come together.", detail.tagline)
        assertEquals("overview", detail.overview)
        assertEquals("2024-07-24", detail.releaseDate)
        assertEquals(128, detail.runtimeMinutes)
        assertEquals(7.6, detail.voteAverage, 0.0)
        assertEquals(5842, detail.voteCount)
        assertEquals(listOf(28, 35), detail.genres.map { it.id })
        assertEquals(listOf("Action", "Comedy"), detail.genres.map { it.name })
    }

    @Test
    fun `asExternalModel builds poster and backdrop urls with different sizes`() {
        val detail = detailResponse.asExternalModel()
        assertTrue(detail.posterUrl!!.endsWith("w500/poster.jpg"))
        assertTrue(detail.backdropUrl!!.endsWith("w780/backdrop.jpg"))
    }

    @Test
    fun `asExternalModel maps a blank image path to null`() {
        val detail = detailResponse.copy(posterPath = "", backdropPath = "").asExternalModel()
        assertNull(detail.posterUrl)
        assertNull(detail.backdropUrl)
    }

    @Test
    fun `CastResult asExternalModel builds a profile url with the profile size`() {
        val member =
            CastResult(
                id = 10859,
                name = "Ryan Reynolds",
                character = "Deadpool",
                profilePath = "/ryan.jpg",
            ).asExternalModel()
        assertEquals(10859, member.id)
        assertEquals("Ryan Reynolds", member.name)
        assertEquals("Deadpool", member.character)
        assertTrue(member.profileUrl!!.endsWith("w185/ryan.jpg"))
    }

    @Test
    fun `CastResult asExternalModel maps a blank profile path to null`() {
        val member =
            CastResult(id = 1667888, name = "Emma Corrin", character = "Cassandra Nova").asExternalModel()
        assertNull(member.profileUrl)
    }

    @Test
    fun `CreditsResponse asExternalModel maps every cast member`() {
        val cast =
            CreditsResponse(
                id = 533535,
                cast =
                listOf(
                    CastResult(id = 10859, name = "Ryan Reynolds", character = "Deadpool", order = 0),
                    CastResult(id = 6968, name = "Hugh Jackman", character = "Wolverine", order = 1),
                ),
            ).asExternalModel()
        assertEquals(2, cast.size)
        assertEquals(listOf("Ryan Reynolds", "Hugh Jackman"), cast.map { it.name })
    }
}
