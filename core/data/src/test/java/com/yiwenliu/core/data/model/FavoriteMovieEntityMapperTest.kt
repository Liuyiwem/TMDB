package com.yiwenliu.core.data.model

import com.yiwenliu.core.data.util.asImagePath
import com.yiwenliu.core.data.util.asImageUrl
import com.yiwenliu.core.database.model.FavoriteMovieEntity
import com.yiwenliu.core.model.FavoriteMovie
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FavoriteMovieEntityMapperTest {
    @Test
    fun `asExternalModel maps each field to its own slot`() {
        val entity = FavoriteMovieEntity(
            id = 533535,
            title = "Deadpool & Wolverine",
            posterPath = "/poster.jpg",
            createdAt = 1_700_000_000_000L,
        )
        val movie = entity.asExternalModel()
        assertEquals(533535, movie.id)
        assertEquals("Deadpool & Wolverine", movie.title)
        assertEquals("/poster.jpg".asImageUrl(), movie.posterUrl)
    }

    @Test
    fun `asEntity keeps createdAt and carries every field across`() {
        val movie = FavoriteMovie(
            id = 533535,
            title = "Deadpool & Wolverine",
            posterUrl = "/poster.jpg".asImageUrl(),
        )
        val entity = movie.asEntity(createdAt = 1_700_000_000_000L)
        assertEquals(533535, entity.id)
        assertEquals("Deadpool & Wolverine", entity.title)
        assertEquals("/poster.jpg", entity.posterPath)
        assertEquals(1_700_000_000_000L, entity.createdAt)
    }

    @Test
    fun `asEntity stores the raw path and asExternalModel restores the url`() {
        val url = "/poster.jpg".asImageUrl()
        val movie = FavoriteMovie(id = 533535, title = "Deadpool & Wolverine", posterUrl = url)
        val entity = movie.asEntity(createdAt = 1_700_000_000_000L)
        assertEquals("/poster.jpg", entity.posterPath)
        assertEquals(url, entity.asExternalModel().posterUrl)
    }

    @Test
    fun `asImagePath leaves an already raw path untouched`() {
        assertEquals("/poster.jpg", "/poster.jpg".asImagePath())
    }

    @Test
    fun `a blank poster path stays blank in both directions`() {
        assertEquals("", "".asImagePath())
        val entity = FavoriteMovieEntity(id = 1, title = "T", posterPath = "", createdAt = 0L)
        assertNull(entity.asExternalModel().posterUrl)
    }
}
