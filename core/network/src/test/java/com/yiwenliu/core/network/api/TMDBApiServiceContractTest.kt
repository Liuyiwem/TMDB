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

class TMDBApiServiceContractTest {
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
                    .body(EMPTY_PAGE_JSON.toResponseBody(JSON_MEDIA_TYPE))
                    .build()
            }.build()

    private val api =
        Retrofit
            .Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(Json.asConverterFactory(JSON_MEDIA_TYPE))
            .build()
            .create(TMDBApiService::class.java)

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

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()

        const val EMPTY_PAGE_JSON = """{"page":1,"results":[],"total_pages":1,"total_results":0}"""
    }
}
