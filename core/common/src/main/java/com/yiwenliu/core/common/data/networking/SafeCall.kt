package com.yiwenliu.core.common.data.networking

import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

suspend inline fun <reified T> safeCall(execute: suspend () -> T): Result<T, NetworkError> = try {
    val result = execute()
    Result.Success(result)
} catch (e: CancellationException) {
    throw e
} catch (e: UnresolvedAddressException) {
    Result.Error(NetworkError.NO_INTERNET)
} catch (e: IOException) {
    Result.Error(NetworkError.NO_INTERNET)
} catch (e: HttpException) {
    handleHttpException(e)
} catch (e: SerializationException) {
    Result.Error(NetworkError.SERIALIZATION)
} catch (_: Exception) {
    Result.Error(NetworkError.UNKNOWN)
}

@PublishedApi
internal fun handleHttpException(exception: HttpException): Result<Nothing, NetworkError> = when (exception.code()) {
    408 -> Result.Error(NetworkError.REQUEST_TIMEOUT)
    429 -> Result.Error(NetworkError.TOO_MANY_REQUESTS)
    in 400..499 -> Result.Error(NetworkError.CLIENT_ERROR)
    in 500..599 -> Result.Error(NetworkError.SERVER_ERROR)
    else -> Result.Error(NetworkError.UNKNOWN)
}
