package com.yiwenliu.core.common.data.networking

import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafeApiCallTest {
    @Test
    fun `safeApiCall returns Success when execution succeeds`() = runTest {
        val expectedData = "test data"
        val result = safeApiCall { expectedData }
        assertTrue(result is Result.Success)
        assertEquals(expectedData, result.data)
    }

    @Test
    fun `safeApiCall returns NO_INTERNET for UnknownHostException`() = runTest {
        val result =
            safeApiCall<String> {
                throw UnknownHostException("api.themoviedb.org")
            }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }

    @Test
    fun `safeApiCall returns NO_INTERNET for IOException`() = runTest {
        val result =
            safeApiCall<String> {
                throw IOException("Network connection failed")
            }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NO_INTERNET, result.error)
    }

    @Test
    fun `safeApiCall returns SERIALIZATION for SerializationException`() = runTest {
        val result =
            safeApiCall<String> {
                throw SerializationException("Failed to parse JSON")
            }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.SERIALIZATION, result.error)
    }

    @Test
    fun `safeApiCall maps an HttpException through its status code`() = runTest {
        val result =
            safeApiCall<String> {
                throw httpExceptionOf(404)
            }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.NOT_FOUND, result.error)
    }

    @Test
    fun `safeApiCall returns UNKNOWN for a generic Exception`() = runTest {
        val result =
            safeApiCall<String> {
                throw RuntimeException("Something went wrong")
            }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Remote.UNKNOWN, result.error)
    }

    @Test
    fun `asRemoteError maps the explicitly handled status codes`() {
        val expected =
            mapOf(
                400 to DataError.Remote.BAD_REQUEST,
                401 to DataError.Remote.UNAUTHORIZED,
                403 to DataError.Remote.FORBIDDEN,
                404 to DataError.Remote.NOT_FOUND,
                408 to DataError.Remote.REQUEST_TIMEOUT,
                409 to DataError.Remote.CONFLICT,
                413 to DataError.Remote.PAYLOAD_TOO_LARGE,
                429 to DataError.Remote.TOO_MANY_REQUESTS,
                500 to DataError.Remote.SERVER_ERROR,
                503 to DataError.Remote.SERVICE_UNAVAILABLE,
            )

        expected.forEach { (code, error) ->
            assertEquals(error, code.asRemoteError(), "Failed for code $code")
        }
    }

    @Test
    fun `asRemoteError falls back to BAD_REQUEST for unlisted 4xx codes`() {
        listOf(402, 418, 422, 451).forEach { code ->
            assertEquals(DataError.Remote.BAD_REQUEST, code.asRemoteError(), "Failed for code $code")
        }
    }

    @Test
    fun `asRemoteError falls back to SERVER_ERROR for unlisted 5xx codes`() {
        listOf(501, 502, 504).forEach { code ->
            assertEquals(DataError.Remote.SERVER_ERROR, code.asRemoteError(), "Failed for code $code")
        }
    }

    @Test
    fun `asRemoteError returns UNKNOWN for codes outside 4xx and 5xx`() {
        listOf(600, 999).forEach { code ->
            assertEquals(DataError.Remote.UNKNOWN, code.asRemoteError(), "Failed for code $code")
        }
    }

    private fun httpExceptionOf(code: Int) = HttpException(
        Response.error<Any>(code, ResponseBody.create(null, "")),
    )
}
