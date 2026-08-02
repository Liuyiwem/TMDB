package com.yiwenliu.core.common.presentation.util

import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError
import org.junit.Test
import kotlin.test.assertEquals

/**
 * 映射表的測試留在 JVM，所以每一次 `testMockDebugUnitTest` 都會跑。
 *
 * 這裡不需要 [android.content.Context]——[toStringResId] 是純函式，回傳的只是資源 id。
 * 真的需要 Context 的那兩個案例（NetworkException 解包、非網路例外的退路）在
 * `src/androidTest` 的同名測試裡。
 */
class NetworkErrorToStringTest {
    /**
     * 這張表刻意獨立於生產碼手寫。生產碼的 when 是窮盡的，所以「漏掉一個 enum」編譯期就
     * 擋下了；這裡守的是另一種錯——新增常數時把資源複製貼上到錯誤的那一個。
     */
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
    fun everyNetworkErrorIsCovered() {
        // 新增 NetworkError 常數卻沒更新這張表時，這一行就會紅。
        assertEquals(NetworkError.entries.toSet(), expected.keys)
    }

    @Test
    fun eachNetworkErrorMapsToItsOwnResId() {
        expected.forEach { (error, resId) ->
            assertEquals(resId, error.toStringResId(), "mapping for $error")
        }
    }

    @Test
    fun distinctErrorsMapToDistinctResIds() {
        val ids = NetworkError.entries.map { it.toStringResId() }
        assertEquals(ids.size, ids.toSet().size)
    }
}
