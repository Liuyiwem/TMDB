package com.yiwenliu.core.common.presentation.util

import android.content.Context
import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException

fun NetworkError.toString(context: Context): String = when (this) {
    NetworkError.REQUEST_TIMEOUT -> context.getString(R.string.error_request_timeout)
    NetworkError.TOO_MANY_REQUESTS -> context.getString(R.string.error_too_many_requests)
    NetworkError.NO_INTERNET -> context.getString(R.string.error_no_internet)
    NetworkError.SERVER_ERROR -> context.getString(R.string.error_server)
    NetworkError.SERIALIZATION -> context.getString(R.string.error_serialization)
    NetworkError.UNKNOWN -> context.getString(R.string.error_unknown)
    NetworkError.CLIENT_ERROR -> context.getString(R.string.error_client)
}

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
