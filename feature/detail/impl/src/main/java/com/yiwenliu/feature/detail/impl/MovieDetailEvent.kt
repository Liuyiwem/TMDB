package com.yiwenliu.feature.detail.impl

import com.yiwenliu.core.ui.util.UiText

internal sealed interface MovieDetailEvent {
    data class ShowError(val message: UiText) : MovieDetailEvent
}
