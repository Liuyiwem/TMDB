package com.yiwenliu.core.ui

import com.yiwenliu.core.model.MovieCategory

object TmdbTestTags {
    const val TAB_ROW = "tabRow"

    const val ERROR = "error"

    const val RETRY = "retry"

    const val APP_BAR_NAV_ICON = "appBar:navIcon"

    const val APP_BAR_TITLE = "appBar:title"

    const val DEFAULT_GRID_PREFIX = "movieGrid"

    const val CONFIRM_DIALOG = "confirmDialog"

    const val CONFIRM_DIALOG_CONFIRM = "confirmDialog:confirm"

    const val CONFIRM_DIALOG_DISMISS = "confirmDialog:dismiss"

    const val MESSAGE_DIALOG = "messageDialog"

    const val MESSAGE_DIALOG_CONFIRM = "messageDialog:confirm"

    fun tab(category: MovieCategory) = "tab:${category.name}"

    fun grid(prefix: String) = "$prefix:grid"

    fun appendLoading(prefix: String) = "$prefix:appendLoading"
}
