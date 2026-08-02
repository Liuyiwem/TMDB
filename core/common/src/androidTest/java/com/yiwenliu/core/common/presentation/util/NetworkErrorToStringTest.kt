package com.yiwenliu.core.common.presentation.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals

/**
 * 只留真正需要 [Context] 的案例。
 *
 * 「哪個 error 對到哪個字串資源」那張表已經抽成純函式 [toStringResId]，測試在
 * `src/test` 的同名檔案裡，跑在 JVM 上、每次 `testMockDebugUnitTest` 都會執行。
 * 這裡剩下的兩個驗的是 [toUserMessage] 的分派，那需要真的解析出字串。
 */
@RunWith(AndroidJUnit4::class)
class NetworkErrorToStringTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun networkExceptionUnwrapsToItsNetworkError() {
        assertEquals(
            context.getString(R.string.error_no_internet),
            NetworkException(NetworkError.NO_INTERNET).toUserMessage(context),
        )
    }

    @Test
    fun nonNetworkThrowableFallsBackToUnknown() {
        assertEquals(
            context.getString(R.string.error_unknown),
            IOException("boom").toUserMessage(context),
        )
    }
}
