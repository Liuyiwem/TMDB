package com.yiwenliu.core.data.util

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import kotlinx.coroutines.CancellationException

internal suspend inline fun <T> safeDatabaseCall(execute: suspend () -> T): Result<T, DataError.Local> = try {
    Result.Success(execute())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.Failure(e.asLocalError())
}
