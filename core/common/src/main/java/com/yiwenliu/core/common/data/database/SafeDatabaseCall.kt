package com.yiwenliu.core.common.data.database

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import kotlinx.coroutines.CancellationException

suspend inline fun <T> safeDatabaseCall(execute: suspend () -> T): Result<T, DataError.Local> = try {
    Result.Success(execute())
} catch (e: CancellationException) {
    throw e
} catch (e: SQLiteFullException) {
    Result.Failure(DataError.Local.DISK_FULL)
} catch (e: SQLiteException) {
    Result.Failure(DataError.Local.UNKNOWN)
}
