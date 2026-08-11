package com.yiwenliu.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TmdbIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
    checkedIcon: @Composable () -> Unit = icon,
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    ) {
        if (checked) checkedIcon() else icon()
    }
}

@Preview(showBackground = true)
@Composable
private fun TmdbIconToggleButtonUncheckedPreview() {
    MaterialTheme {
        TmdbIconToggleButton(
            checked = false,
            onCheckedChange = {},
            icon = { Icon(Icons.Rounded.FavoriteBorder, contentDescription = null) },
            checkedIcon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TmdbIconToggleButtonCheckedPreview() {
    MaterialTheme {
        TmdbIconToggleButton(
            checked = true,
            onCheckedChange = {},
            icon = { Icon(Icons.Rounded.FavoriteBorder, contentDescription = null) },
            checkedIcon = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
        )
    }
}
