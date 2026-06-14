package com.yiwenliu.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

internal class AuthInterceptor(
    private val apiToken: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val authenticatedRequest =
            originalRequest
                .newBuilder()
                .header("Authorization", "Bearer $apiToken")
                .header("accept", "application/json")
                .build()

        return chain.proceed(authenticatedRequest)
    }
}
