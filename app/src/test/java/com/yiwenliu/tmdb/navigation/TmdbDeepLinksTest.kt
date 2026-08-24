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
            listOf(HomeNavKey, MovieDetailNavKey(movieId = 1241982)),
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
    fun `these links resolve to nothing`() {
        val rejected: List<Pair<String, String?>> = listOf(
            "a null link" to null,
            "a blank link" to "",
            "another scheme" to "https://movie?id=550",
            "an unknown host" to "tmdb://settings",
            "a missing host" to "tmdb://",
            "a nested path" to "tmdb://movie/detail?id=550",
            "a movie link without an id" to "tmdb://movie",
            "a movie link with an empty id" to "tmdb://movie?id=",
            "a non numeric id" to "tmdb://movie?id=abc",
            "a zero id" to "tmdb://movie?id=0",
            "a negative id" to "tmdb://movie?id=-1",
            "an overflowing id" to "tmdb://movie?id=99999999999999",
            "a misnamed id parameter" to "tmdb://movie?movieId=550",
            "a parameter merely starting with id" to "tmdb://movie?identifier=550",
            "a malformed link" to "not a uri at all",
            "a link with an unencoded space" to "tmdb://movie?id=5 5",
        )
        rejected.forEach { (label, uri) ->
            assertTrue(TmdbDeepLinks.parse(uri).isEmpty(), "Expected no destination for $label: $uri")
        }
    }
}
