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
                .url("https://api.invalid/3/movie/popular")
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
        val sent = execute { header("Authorization", "Bearer stale") }
        assertEquals(listOf("Bearer $TOKEN"), sent.headers.values("Authorization"))
    }

    @Test
    fun `preserves unrelated headers and adds only the two it owns`() {
        val sent = execute { header("X-Trace-Id", "abc123") }
        assertNull(sent.body)
        assertEquals("abc123", sent.header("X-Trace-Id"))
        assertEquals(
            listOf("accept", "Authorization", "X-Trace-Id"),
            sent.headers.names().toList(),
        )
    }

    private companion object {
        const val TOKEN = "test-token"
    }
}
