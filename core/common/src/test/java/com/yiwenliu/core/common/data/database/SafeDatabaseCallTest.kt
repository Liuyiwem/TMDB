package com.yiwenliu.core.common.data.database

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SafeDatabaseCallTest {
    @Test
    fun `safeDatabaseCall returns Success when execution succeeds`() = runTest {
        val result = safeDatabaseCall { "written" }
        assertTrue(result is Result.Success)
        assertEquals("written", result.data)
    }

    @Test
    fun `safeDatabaseCall returns DISK_FULL for SQLiteFullException`() = runTest {
        val result = safeDatabaseCall<Unit> { throw SQLiteFullException("disk full") }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `safeDatabaseCall returns UNKNOWN for other SQLiteExceptions`() = runTest {
        val result = safeDatabaseCall<Unit> { throw SQLiteException("boom") }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `safeDatabaseCall rethrows CancellationException`() = runTest {
        assertFailsWith<CancellationException> {
            safeDatabaseCall<Unit> { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `safeDatabaseCall returns UNKNOWN for non-database exceptions`() = runTest {
        val result = safeDatabaseCall<Unit> { throw IllegalStateException("not a database problem") }
        assertTrue(result is Result.Failure)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }
}
