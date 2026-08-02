package com.yiwenliu.core.common.presentation.util

import android.content.Context
import androidx.annotation.StringRes
import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException

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

fun Throwable.toUserMessage(context: Context): String = when (this) {
    is NetworkException -> networkError.toString(context)
    else -> context.getString(R.string.error_unknown)
}
