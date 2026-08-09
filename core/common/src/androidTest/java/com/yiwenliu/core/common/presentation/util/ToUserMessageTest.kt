package com.yiwenliu.core.common.presentation.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yiwenliu.core.common.R
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ToUserMessageTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun networkExceptionUnwrapsToItsNetworkError() {
        assertEquals(
            context.getString(R.string.error_no_internet),
            NetworkException(NetworkError.NO_INTERNET).toUserMessage(context),
        )
    }

    @Test
    fun nonNetworkThrowableFallsBackToUnknown() {
        assertEquals(
            context.getString(R.string.error_unknown),
            IOException("boom").toUserMessage(context),
        )
    }
}
