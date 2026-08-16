package com.yiwenliu.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

val LocalTopAppBarTitleOverrides = staticCompositionLocalOf { mutableStateMapOf<NavKey, String>() }

@Composable
fun rememberTopAppBarTitleOverrides(): SnapshotStateMap<NavKey, String> = remember { mutableStateMapOf() }
