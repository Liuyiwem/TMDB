package com.yiwenliu.core.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource

@Immutable
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    data class StringResource(@param:StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(id, *args.toTypedArray())
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(id, *args.toTypedArray())
    }
}
