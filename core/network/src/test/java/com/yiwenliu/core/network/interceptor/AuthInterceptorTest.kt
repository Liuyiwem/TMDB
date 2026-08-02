package com.yiwenliu.core.network.interceptor

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthInterceptorTest {
    private val sentRequests = mutableListOf<Request>()

    private val client =
        OkHttpClient
            .Builder()
            .addInterceptor(AuthInterceptor(TOKEN))
            // 終端 interceptor：記下請求後直接合成回應，不呼叫 chain.proceed()。
            // 沒有 socket、沒有 MockWebServer、沒有等待。
            .addInterceptor { chain ->
                sentRequests += chain.request()
                Response
                    .Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody())
                    .build()
            }.build()

    private fun execute(build: Request.Builder.() -> Unit = {}): Request {
        val request =
            Request
                .Builder()
                .url("https://api.themoviedb.org/3/movie/popular")
                .apply(build)
                .build()
        client.newCall(request).execute().close()
        return sentRequests.last()
    }

    @Test
    fun `attaches the bearer token and the accept header`() {
        val sent = execute()

        assertEquals("Bearer $TOKEN", sent.header("Authorization"))
        assertEquals("application/json", sent.header("accept"))
    }

    @Test
    fun `replaces a pre-existing Authorization header instead of appending one`() {
        // 生產碼用 header() 而非 addHeader()。改成 addHeader() 會送出兩個 Authorization，
        // TMDB 直接回 401——而且只在真的打網路時才看得到。
        val sent = execute { header("Authorization", "Bearer stale") }

        assertEquals(listOf("Bearer $TOKEN"), sent.headers.values("Authorization"))
    }

    @Test
    fun `preserves unrelated headers and adds only the two it owns`() {
        val sent = execute { header("X-Trace-Id", "abc123") }

        assertNull(sent.body)
        assertEquals("abc123", sent.header("X-Trace-Id"))
        // 釘住「只加兩個、不動別人的」。原本這裡斷言的是 method/url/body 沒被改動，
        // 但 newBuilder() 會原樣複製那三樣、header() 只寫 header map，
        // 結構上就不可能改到——那是恆真的斷言，想不出任何會讓它紅的變異。
        // 改成比對 header 名稱：多加一個 User-Agent、或誤用 addHeader 造成重複，
        // 都會在這裡對不上。
        //
        // names() 回傳的是 case-insensitive 排序的 TreeSet（不是插入順序），
        // 所以這個清單比對的是「有哪些 header」而不是「以什麼順序加入」——
        // 對 interceptor 內部的實作順序不敏感，正是我們要的。
        assertEquals(
            listOf("accept", "Authorization", "X-Trace-Id"),
            sent.headers.names().toList(),
        )
    }

    private companion object {
        const val TOKEN = "test-token"
    }
}
