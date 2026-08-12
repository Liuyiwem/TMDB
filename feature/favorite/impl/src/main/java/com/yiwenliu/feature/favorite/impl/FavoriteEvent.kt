package com.yiwenliu.feature.favorite.impl

import com.yiwenliu.core.ui.util.UiText

internal sealed interface FavoriteEvent {
    data class ShowError(val message: UiText) : FavoriteEvent
}
