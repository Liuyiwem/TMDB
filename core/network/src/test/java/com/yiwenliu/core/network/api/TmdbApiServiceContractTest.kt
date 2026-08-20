package com.yiwenliu.core.network.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Retrofit
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbApiServiceContractTest {
    private val sentRequests = mutableListOf<Request>()

    private val client =
        OkHttpClient
            .Builder()
            .addInterceptor { chain ->
                sentRequests += chain.request()
                Response
                    .Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(EMPTY_RESPONSE_JSON.toResponseBody(JSON_MEDIA_TYPE))
                    .build()
            }.build()

    private val api =
        Retrofit
            .Builder()
            .baseUrl("https://api.invalid/3/")
            .client(client)
            .addConverterFactory(NETWORK_JSON.asConverterFactory(JSON_MEDIA_TYPE))
            .build()
            .create(TmdbApiService::class.java)

    @Test
    fun `getMoviesByCategory builds movie-category with a page query`() = runTest {
        api.getMoviesByCategory("now_playing", page = 2)
        val url = sentRequests.last().url
        assertEquals("/3/movie/now_playing", url.encodedPath)
        assertEquals("2", url.queryParameter("page"))
    }

    @Test
    fun `searchMovies url-encodes the query`() = runTest {
        api.searchMovies("star wars", page = 1)
        val url = sentRequests.last().url
        assertEquals("/3/search/movie", url.encodedPath)
        assertEquals("query=star%20wars&page=1", url.encodedQuery)
    }

    @Test
    fun `getMovieDetail builds movie by id without a page query`() = runTest {
        api.getMovieDetail(533535)
        val url = sentRequests.last().url
        assertEquals("/3/movie/533535", url.encodedPath)
        assertNull(url.queryParameter("page"))
    }

    @Test
    fun `getMovieCredits builds the credits path`() = runTest {
        api.getMovieCredits(533535)
        assertEquals("/3/movie/533535/credits", sentRequests.last().url.encodedPath)
    }

    @Test
    fun `getMovieRecommendations defaults to page 1`() = runTest {
        api.getMovieRecommendations(533535)
        val url = sentRequests.last().url
        assertEquals("/3/movie/533535/recommendations", url.encodedPath)
        assertEquals("1", url.queryParameter("page"))
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()

        val NETWORK_JSON = Json { ignoreUnknownKeys = true }

        const val EMPTY_RESPONSE_JSON = """{"id":0,"results":[],"total_pages":1}"""
    }
}
