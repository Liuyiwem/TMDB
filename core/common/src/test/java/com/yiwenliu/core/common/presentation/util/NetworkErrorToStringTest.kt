package com.yiwenliu.core.common.presentation.util

import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError
import org.junit.Test
import kotlin.test.assertEquals

class NetworkErrorToStringTest {
    private val expected =
        mapOf(
            NetworkError.REQUEST_TIMEOUT to R.string.error_request_timeout,
            NetworkError.TOO_MANY_REQUESTS to R.string.error_too_many_requests,
            NetworkError.NO_INTERNET to R.string.error_no_internet,
            NetworkError.SERVER_ERROR to R.string.error_server,
            NetworkError.SERIALIZATION to R.string.error_serialization,
            NetworkError.UNKNOWN to R.string.error_unknown,
            NetworkError.CLIENT_ERROR to R.string.error_client,
        )

    @Test
    fun `toStringResId everyError returns Mapping`() {
        assertEquals(NetworkError.entries.toSet(), expected.keys)
    }

    @Test
    fun `toStringResId eachError returns ExpectedResId`() {
        expected.forEach { (error, resId) ->
            assertEquals(resId, error.toStringResId(), "mapping for $error")
        }
    }

    @Test
    fun `toStringResId distinctErrors returns DistinctResIds`() {
        val ids = NetworkError.entries.map { it.toStringResId() }
        assertEquals(ids.size, ids.toSet().size)
    }
}
