package com.yiwenliu.feature.favorite.impl

import com.yiwenliu.core.model.FavoriteMovie

internal sealed interface FavoriteAction {
    data class OnRemoveClick(val movie: FavoriteMovie) : FavoriteAction

    data object OnRemoveConfirm : FavoriteAction

    data object OnRemoveDismiss : FavoriteAction

    data object OnErrorDismiss : FavoriteAction
}
