package com.yiwenliu.core.data.testdoubles

import com.yiwenliu.core.network.api.TmdbApiService
import com.yiwenliu.core.network.mock.MockTmdbApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json

@OptIn(ExperimentalCoroutinesApi::class)
class TestTmdbApiService(
    private val source: MockTmdbApiService =
        MockTmdbApiService(
            ioDispatcher = UnconfinedTestDispatcher(),
            networkJson =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            },
        ),
) : TmdbApiService by source {
    var errorToThrow: Exception?
        get() = source.errorToThrow
        set(value) {
            source.errorToThrow = value
        }
}
