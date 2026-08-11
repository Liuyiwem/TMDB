package com.yiwenliu.core.common.data.networking

import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

suspend inline fun <reified T> safeApiCall(execute: suspend () -> T): Result<T, DataError.Remote> = try {
    Result.Success(execute())
} catch (e: CancellationException) {
    throw e
} catch (e: UnresolvedAddressException) {
    Result.Failure(DataError.Remote.NO_INTERNET)
} catch (e: IOException) {
    Result.Failure(DataError.Remote.NO_INTERNET)
} catch (e: HttpException) {
    Result.Failure(e.code().asRemoteError())
} catch (e: SerializationException) {
    Result.Failure(DataError.Remote.SERIALIZATION)
} catch (_: Exception) {
    Result.Failure(DataError.Remote.UNKNOWN)
}

@PublishedApi
internal fun Int.asRemoteError(): DataError.Remote = when (this) {
    400 -> DataError.Remote.BAD_REQUEST
    401 -> DataError.Remote.UNAUTHORIZED
    403 -> DataError.Remote.FORBIDDEN
    404 -> DataError.Remote.NOT_FOUND
    408 -> DataError.Remote.REQUEST_TIMEOUT
    409 -> DataError.Remote.CONFLICT
    413 -> DataError.Remote.PAYLOAD_TOO_LARGE
    429 -> DataError.Remote.TOO_MANY_REQUESTS
    500 -> DataError.Remote.SERVER_ERROR
    503 -> DataError.Remote.SERVICE_UNAVAILABLE
    in 400..499 -> DataError.Remote.BAD_REQUEST
    in 500..599 -> DataError.Remote.SERVER_ERROR
    else -> DataError.Remote.UNKNOWN
}
