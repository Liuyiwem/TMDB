package com.yiwenliu.core.common.data.networking

import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.nio.channels.UnresolvedAddressException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafeCallTest {
    @Test
    fun `safeCall returns Success when execution succeeds`() = runTest {
        val expectedData = "test data"

        val result = safeCall { expectedData }

        assertTrue(result is Result.Success)
        assertEquals(expectedData, result.data)
    }

    @Test
    fun `safeCall returns NO_INTERNET error for UnresolvedAddressException`() = runTest {
        val result =
            safeCall<String> {
                throw UnresolvedAddressException()
            }

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.NO_INTERNET, result.error)
    }

    @Test
    fun `safeCall returns NO_INTERNET error for IOException`() = runTest {
        val result =
            safeCall<String> {
                throw IOException("Network connection failed")
            }

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.NO_INTERNET, result.error)
    }

    @Test
    fun `safeCall returns SERIALIZATION error for SerializationException`() = runTest {
        val result =
            safeCall<String> {
                throw SerializationException("Failed to parse JSON")
            }

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.SERIALIZATION, result.error)
    }

    @Test
    fun `safeCall handles HttpException and delegates to handleHttpException`() = runTest {
        val httpException =
            HttpException(
                Response.error<Any>(404, okhttp3.ResponseBody.create(null, "Not Found")),
            )

        val result =
            safeCall<String> {
                throw httpException
            }

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.CLIENT_ERROR, result.error)
    }

    @Test
    fun `safeCall returns UNKNOWN error for generic Exception`() = runTest {
        val result =
            safeCall<String> {
                throw RuntimeException("Something went wrong")
            }

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.UNKNOWN, result.error)
    }

    @Test
    fun `handleHttpException returns REQUEST_TIMEOUT for 408`() {
        val httpException =
            HttpException(
                Response.error<Any>(408, okhttp3.ResponseBody.create(null, "")),
            )

        val result = handleHttpException(httpException)

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.REQUEST_TIMEOUT, result.error)
    }

    @Test
    fun `handleHttpException returns TOO_MANY_REQUESTS for 429`() {
        val httpException =
            HttpException(
                Response.error<Any>(429, okhttp3.ResponseBody.create(null, "")),
            )

        val result = handleHttpException(httpException)

        assertTrue(result is Result.Error)
        assertEquals(NetworkError.TOO_MANY_REQUESTS, result.error)
    }

    @Test
    fun `handleHttpException returns CLIENT_ERROR for 4xx codes`() {
        listOf(400, 401, 403, 404, 422).forEach { code ->
            val httpException =
                HttpException(
                    Response.error<Any>(code, okhttp3.ResponseBody.create(null, "")),
                )

            val result = handleHttpException(httpException)

            assertTrue(result is Result.Error, "Failed for code $code")
            assertEquals(NetworkError.CLIENT_ERROR, result.error, "Failed for code $code")
        }
    }

    @Test
    fun `handleHttpException returns SERVER_ERROR for 5xx codes`() {
        listOf(500, 501, 502, 503, 504).forEach { code ->
            val httpException =
                HttpException(
                    Response.error<Any>(code, okhttp3.ResponseBody.create(null, "")),
                )

            val result = handleHttpException(httpException)

            assertTrue(result is Result.Error, "Failed for code $code")
            assertEquals(NetworkError.SERVER_ERROR, result.error, "Failed for code $code")
        }
    }

    @Test
    fun `handleHttpException returns UNKNOWN for unexpected codes`() {
        listOf(600, 999).forEach { code ->
            val httpException =
                HttpException(
                    Response.error<Any>(code, okhttp3.ResponseBody.create(null, "")),
                )

            val result = handleHttpException(httpException)

            assertTrue(result is Result.Error, "Failed for code $code")
            assertEquals(NetworkError.UNKNOWN, result.error, "Failed for code $code")
        }
    }
}
