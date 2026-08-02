package com.yiwenliu.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TmdbNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    bounceHeight: Dp = 6.dp,
) {
    val bounce = remember { Animatable(0f) }
    val density = LocalDensity.current
    LaunchedEffect(selected) {
        if (selected) {
            val up = with(density) { bounceHeight.toPx() }
            bounce.animateTo(-up, spring(stiffness = Spring.StiffnessLow))
            bounce.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        } else {
            bounce.animateTo(0f)
        }
    }

    // 刻意【不】用 by 委派。用 by 的話 animateColor 會在下面的 Column 內容裡被讀取，
    // 而 Column 是 inline、不產生自己的重啟範圍，於是那個讀取被記在 TmdbNavItem 自己身上
    // ——動畫的每一帧都會重跑整個函式本體（tween 500ms、60fps ≈ 30 次／項目，
    // 而一次點擊有兩個項目在動）。
    //
    // 保持它是 State、只在 ColorProducer 裡讀，顏色就變成【繪製階段】的讀取：
    // 每帧只重畫，不重組、不重新量測。跟下面 bounce 用 graphicsLayer 的 lambda 版同一個道理。
    val contentColor =
        animateColorAsState(
            targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            animationSpec = tween(durationMillis = 500),
            label = "navLabelColor",
        )
    val contentColorProducer = remember(contentColor) { ColorProducer { contentColor.value } }

    val interactionSource = remember { MutableInteractionSource() }
    val iconPainter = rememberVectorPainter(icon)

    Column(
        modifier =
        modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = iconPainter,
            tint = contentColorProducer,
            contentDescription = label,
            modifier = Modifier.graphicsLayer { translationY = bounce.value },
        )
        Spacer(Modifier.height(4.dp))
        // 用 BasicText 而不是 Text：只有 BasicText 收 ColorProducer。
        // 這裡沒有損失——style 本來就顯式傳入（LocalTextStyle 本來也沒參與合併），
        // 顏色本來就覆寫（LocalContentColor 本來也沒參與）。
        BasicText(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = contentColorProducer,
        )
    }
}

@Preview
@Composable
private fun TmdbNavItemBarPreview() {
    MaterialTheme {
        ShortNavigationBar {
            TmdbNavItem(
                selected = false,
                onClick = {},
                icon = Icons.Rounded.Search,
                label = "Search",
            )
            TmdbNavItem(
                selected = true,
                onClick = {},
                icon = Icons.Rounded.Home,
                label = "Home",
            )
            TmdbNavItem(
                selected = false,
                onClick = {},
                icon = Icons.Rounded.Favorite,
                label = "Favorite",
            )
        }
    }
}

@Preview
@Composable
private fun TmdbNavItemRailPreview() {
    MaterialTheme {
        NavigationRail {
            TmdbNavItem(
                selected = false,
                onClick = {},
                icon = Icons.Rounded.Search,
                label = "Search",
            )
            TmdbNavItem(
                selected = true,
                onClick = {},
                icon = Icons.Rounded.Home,
                label = "Home",
            )
            TmdbNavItem(
                selected = false,
                onClick = {},
                icon = Icons.Rounded.Favorite,
                label = "Favorite",
            )
        }
    }
}
