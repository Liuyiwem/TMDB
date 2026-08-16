package com.yiwenliu.tmdb.navigation

import com.yiwenliu.feature.detail.api.navigation.MovieDetailNavKey
import com.yiwenliu.feature.home.api.navigation.HomeNavKey
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TmdbDeepLinksTest {
    @Test
    fun `home link resolves to the home stack`() {
        assertEquals(listOf(HomeNavKey), TmdbDeepLinks.parse("tmdb://home"))
    }

    @Test
    fun `movie link resolves to home then detail`() {
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 1241982, title = "")),
            TmdbDeepLinks.parse("tmdb://movie?id=1241982"),
        )
    }

    @Test
    fun `home link tolerates a trailing slash`() {
        assertEquals(listOf(HomeNavKey), TmdbDeepLinks.parse("tmdb://home/"))
    }

    @Test
    fun `movie link tolerates a trailing slash`() {
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 550)),
            TmdbDeepLinks.parse("tmdb://movie/?id=550"),
        )
    }

    @Test
    fun `scheme and host are case insensitive`() {
        assertEquals(listOf(HomeNavKey), TmdbDeepLinks.parse("TMDB://HOME"))
    }

    @Test
    fun `movie link ignores a preceding unrelated parameter`() {
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 550)),
            TmdbDeepLinks.parse("tmdb://movie?utm_source=mail&id=550"),
        )
    }

    @Test
    fun `movie link ignores a trailing unrelated parameter`() {
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 550)),
            TmdbDeepLinks.parse("tmdb://movie?id=550&utm_source=mail"),
        )
    }

    @Test
    fun `movie link takes the first id when it is repeated`() {
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 550)),
            TmdbDeepLinks.parse("tmdb://movie?id=550&id=999"),
        )
    }

    @Test
    fun `movie link ignores a fragment`() {
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 550)),
            TmdbDeepLinks.parse("tmdb://movie?id=550#cast"),
        )
    }

    @Test
    fun `movie link decodes a percent encoded id`() {
        assertEquals(
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 550)),
            TmdbDeepLinks.parse("tmdb://movie?id=%3550"),
        )
    }

    @Test
    fun `null link resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse(null).isEmpty())
    }

    @Test
    fun `blank link resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("").isEmpty())
    }

    @Test
    fun `another scheme resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("https://movie?id=550").isEmpty())
    }

    @Test
    fun `an unknown host resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://settings").isEmpty())
    }

    @Test
    fun `a missing host resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://").isEmpty())
    }

    @Test
    fun `a nested path resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie/detail?id=550").isEmpty())
    }

    @Test
    fun `a movie link without an id resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie").isEmpty())
    }

    @Test
    fun `a movie link with an empty id resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?id=").isEmpty())
    }

    @Test
    fun `a non numeric id resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?id=abc").isEmpty())
    }

    @Test
    fun `a zero id resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?id=0").isEmpty())
    }

    @Test
    fun `a negative id resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?id=-1").isEmpty())
    }

    @Test
    fun `an overflowing id resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?id=99999999999999").isEmpty())
    }

    @Test
    fun `a misnamed id parameter resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?movieId=550").isEmpty())
    }

    @Test
    fun `a parameter merely starting with id resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?identifier=550").isEmpty())
    }

    @Test
    fun `a malformed link resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("not a uri at all").isEmpty())
    }

    @Test
    fun `a link with an unencoded space resolves to nothing`() {
        assertTrue(TmdbDeepLinks.parse("tmdb://movie?id=5 5").isEmpty())
    }
}
