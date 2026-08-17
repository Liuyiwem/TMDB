package com.yiwenliu.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private object TestTitleKey : NavKey

private object TestOtherTitleKey : NavKey

class TopAppBarSpecTest {
    @Test
    fun `resolveTitleOverride returns the override of the given key`() {
        assertEquals(
            "From override",
            resolveTitleOverride(TestTitleKey, mapOf(TestTitleKey to "From override")),
        )
    }

    @Test
    fun `resolveTitleOverride is null without an override`() {
        assertNull(resolveTitleOverride(TestTitleKey, emptyMap()))
    }

    @Test
    fun `resolveTitleOverride is null when the override is blank`() {
        assertNull(resolveTitleOverride(TestTitleKey, mapOf(TestTitleKey to "  ")))
    }

    @Test
    fun `resolveTitleOverride only reads the override of the given key`() {
        assertNull(resolveTitleOverride(TestTitleKey, mapOf(TestOtherTitleKey to "From another key")))
    }
}
