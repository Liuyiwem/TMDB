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

/**
 * 釘住 [TMDBApiService] 的 Retrofit 註解實際組出什麼 URL。
 *
 * 這也是「MovieCategory.path 同時是 URL 片段與 mock asset 檔名前綴」這個隱含耦合的
 * 真正解法：路徑組法一旦被改壞，這裡就會紅，而不是等到打真的網路才發現。
 */
class TMDBApiServiceContractTest {
    private val sentRequests = mutableListOf<Request>()

    private val client =
        OkHttpClient
            .Builder()
            // 終端 interceptor：記下請求後直接合成回應，不呼叫 chain.proceed()。
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
            // 硬寫 baseUrl 而不是讀 BuildConfig：encodedPath 的斷言要對得上，
            // 而且 core:network 的 BuildConfig 值來自 local.properties，測試不該依賴它。
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(Json.asConverterFactory(JSON_MEDIA_TYPE))
            .build()
            .create(TMDBApiService::class.java)

    @Test
    fun `getMoviesByCategory builds movie-category with a page query`() = runTest {
        // Retrofit 不吃 interface 上的 Kotlin 預設值，所以 page 一定要顯式傳。
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
        // 斷言 encodedQuery 而不是 queryParameter()：後者回傳【解碼後】的值，
        // 所以無論 wire 上是 star%20wars、star+wars 還是原始空格，它都回 "star wars"
        // ——把 @Query 改成 encoded = true 也照樣綠，等於沒驗到編碼。
        assertEquals("query=star%20wars&page=1", url.encodedQuery)
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()

        // core:model 不是 core:network 的依賴，所以類別名硬寫 "now_playing"。
        const val EMPTY_PAGE_JSON = """{"page":1,"results":[],"total_pages":1,"total_results":0}"""
    }
}
