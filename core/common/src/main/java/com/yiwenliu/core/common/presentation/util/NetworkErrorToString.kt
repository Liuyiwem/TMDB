package com.yiwenliu.core.common.presentation.util

import android.content.Context
import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError

fun NetworkError.toString(context: Context): String =
    when (this) {
        NetworkError.REQUEST_TIMEOUT -> context.getString(R.string.error_request_timeout)
        NetworkError.TOO_MANY_REQUESTS -> context.getString(R.string.error_too_many_requests)
        NetworkError.NO_INTERNET -> context.getString(R.string.error_no_internet)
        NetworkError.SERVER_ERROR -> context.getString(R.string.error_server)
        NetworkError.SERIALIZATION -> context.getString(R.string.error_serialization)
        NetworkError.UNKNOWN -> context.getString(R.string.error_unknown)
        NetworkError.CLIENT_ERROR -> context.getString(R.string.error_client)
    }
