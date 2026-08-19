package com.yiwenliu.core.data.util

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal fun <T> Flow<T>.asDatabaseResult(): Flow<Result<T, DataError.Local>> =
    map<T, Result<T, DataError.Local>> { Result.Success(it) }
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            emit(Result.Failure(throwable.asLocalError()))
        }
