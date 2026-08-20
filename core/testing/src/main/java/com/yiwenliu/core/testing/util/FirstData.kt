package com.yiwenliu.core.testing.util

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.test.assertIs

suspend fun <T> Flow<Result<T, DataError.Local>>.firstData(): T = assertIs<Result.Success<T>>(first()).data
