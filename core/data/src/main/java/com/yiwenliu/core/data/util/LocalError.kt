package com.yiwenliu.core.data.util

import android.database.sqlite.SQLiteFullException
import com.yiwenliu.core.common.domain.util.DataError

internal fun Throwable.asLocalError(): DataError.Local = when (this) {
    is SQLiteFullException -> DataError.Local.DISK_FULL
    else -> DataError.Local.UNKNOWN
}
