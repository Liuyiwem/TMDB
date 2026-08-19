package com.yiwenliu.core.ui.util

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.DataErrorException
import com.yiwenliu.core.ui.R
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

class DataErrorToUiTextTest {
    @Test
    fun `every remote error maps to a distinct message where one exists`() {
        val expected =
            mapOf(
                DataError.Remote.BAD_REQUEST to R.string.error_bad_request,
                DataError.Remote.UNAUTHORIZED to R.string.error_unauthorized,
                DataError.Remote.FORBIDDEN to R.string.error_forbidden,
                DataError.Remote.NOT_FOUND to R.string.error_not_found,
                DataError.Remote.REQUEST_TIMEOUT to R.string.error_request_timeout,
                DataError.Remote.CONFLICT to R.string.error_conflict,
                DataError.Remote.PAYLOAD_TOO_LARGE to R.string.error_payload_too_large,
                DataError.Remote.TOO_MANY_REQUESTS to R.string.error_too_many_requests,
                DataError.Remote.NO_INTERNET to R.string.error_no_internet,
                DataError.Remote.SERVER_ERROR to R.string.error_server,
                DataError.Remote.SERVICE_UNAVAILABLE to R.string.error_service_unavailable,
                DataError.Remote.SERIALIZATION to R.string.error_serialization,
                DataError.Remote.UNKNOWN to R.string.error_unknown,
            )
        expected.forEach { (error, stringRes) ->
            assertEquals(UiText.StringResource(stringRes), error.toUiText(), "Failed for $error")
        }
    }

    @Test
    fun `every local error maps to a message`() {
        val expected =
            mapOf(
                DataError.Local.DISK_FULL to R.string.error_disk_full,
                DataError.Local.UNKNOWN to R.string.error_unknown,
            )
        expected.forEach { (error, stringRes) ->
            assertEquals(UiText.StringResource(stringRes), error.toUiText(), "Failed for $error")
        }
    }

    @Test
    fun `a DataErrorException unwraps to its underlying error`() {
        val throwable = DataErrorException(DataError.Remote.NO_INTERNET)
        assertEquals(UiText.StringResource(R.string.error_no_internet), throwable.toUiText())
    }

    @Test
    fun `an unrecognised throwable falls back to the unknown message`() {
        val throwable = IOException("boom")
        assertEquals(UiText.StringResource(R.string.error_unknown), throwable.toUiText())
    }
}
