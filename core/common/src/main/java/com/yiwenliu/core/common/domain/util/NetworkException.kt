package com.yiwenliu.core.common.domain.util

class NetworkException(
    val networkError: NetworkError,
) : Exception(networkError.name)
