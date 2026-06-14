package com.yiwenliu.core.common.domain.util

enum class NetworkError : Error {
    REQUEST_TIMEOUT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    SERVER_ERROR,
    CLIENT_ERROR,
    SERIALIZATION,
    UNKNOWN,
}
