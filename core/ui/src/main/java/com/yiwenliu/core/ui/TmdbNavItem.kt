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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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

    val animateColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 500),
        label = "navLabelColor",
    )

    val interactionSource = remember { MutableInteractionSource() }

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
            imageVector = icon,
            contentDescription = label,
            tint = animateColor,
            modifier = Modifier.graphicsLayer { translationY = bounce.value },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = animateColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
