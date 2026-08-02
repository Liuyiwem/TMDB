package com.yiwenliu.core.common.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException

/**
 * 錯誤 → 字串資源的對應表。
 *
 * 刻意抽成不吃 [Context] 的純函式：真正會出錯的是這張表本身（新增常數時把資源
 * 複製貼上到錯誤的那一個），而那個風險不需要 Android runtime 就能測。
 * 抽出來之後映射表的測試可以留在 `src/test`，每次 `testMockDebugUnitTest` 都會跑；
 * 需要 [Context] 的只剩下最後那一次 getString——那是 Android 的責任，不是這裡的。
 */
@StringRes
fun NetworkError.toStringResId(): Int = when (this) {
    NetworkError.REQUEST_TIMEOUT -> R.string.error_request_timeout
    NetworkError.TOO_MANY_REQUESTS -> R.string.error_too_many_requests
    NetworkError.NO_INTERNET -> R.string.error_no_internet
    NetworkError.SERVER_ERROR -> R.string.error_server
    NetworkError.SERIALIZATION -> R.string.error_serialization
    NetworkError.UNKNOWN -> R.string.error_unknown
    NetworkError.CLIENT_ERROR -> R.string.error_client
}

fun NetworkError.toString(context: Context): String = context.getString(toStringResId())

/**
 * 把任意 [Throwable] 轉成可顯示給使用者的訊息。
 *
 * Paging 的 `LoadState.Error` 給的是 `Throwable`，所以每個顯示錯誤的畫面都需要這一層包裝。
 * 這裡刻意不是 `@Composable`：`core:common` 沒有套用 compose 外掛，放不了 composable，
 * 而吃 `Context` 也讓它能在非 Compose 的地方重用。
 */
fun Throwable.toUserMessage(context: Context): String = when (this) {
    is NetworkException -> networkError.toString(context)
    else -> context.getString(R.string.error_unknown)
}
