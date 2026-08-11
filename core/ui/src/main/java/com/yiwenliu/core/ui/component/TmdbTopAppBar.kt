package com.yiwenliu.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.yiwenliu.core.navigation.NavIcon
import com.yiwenliu.core.ui.R
import com.yiwenliu.core.ui.TmdbTestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TmdbTopAppBar(title: String, navIcon: NavIcon, onNavIconClick: () -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(TmdbTestTags.APP_BAR_TITLE),
            )
        },
        navigationIcon = {
            navIcon.toUi()?.let { (image, descriptionRes) ->
                IconButton(
                    onClick = onNavIconClick,
                    modifier = Modifier.testTag(TmdbTestTags.APP_BAR_NAV_ICON),
                ) {
                    Icon(
                        imageVector = image,
                        contentDescription = stringResource(descriptionRes),
                    )
                }
            }
        },
        modifier = modifier,
    )
}

private data class NavIconUi(val image: ImageVector, @param:StringRes val descriptionRes: Int)

private fun NavIcon.toUi(): NavIconUi? = when (this) {
    NavIcon.None -> null
    NavIcon.Back -> NavIconUi(Icons.AutoMirrored.Rounded.ArrowBack, R.string.nav_back)
    NavIcon.Close -> NavIconUi(Icons.Rounded.Close, R.string.nav_close)
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun TmdbTopAppBarPreview(@PreviewParameter(NavIconPreviewParameterProvider::class) navIcon: NavIcon) {
    MaterialTheme {
        TmdbTopAppBar(
            title = "Deadpool & Wolverine",
            navIcon = navIcon,
            onNavIconClick = {},
        )
    }
}

private class NavIconPreviewParameterProvider : PreviewParameterProvider<NavIcon> {
    override val values: Sequence<NavIcon> = NavIcon.entries.asSequence()
}
