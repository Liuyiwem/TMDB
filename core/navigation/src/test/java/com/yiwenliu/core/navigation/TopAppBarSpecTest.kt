package com.yiwenliu.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Test
import kotlin.test.assertEquals

private object TestTitleKey : NavKey

private object TestOtherTitleKey : NavKey

class TopAppBarSpecTest {
    @Test
    fun `resolveTitle prefers the key title over the override`() {
        val spec = TopAppBarSpec(title = { "From key" })
        assertEquals("From key", spec.resolveTitle(TestTitleKey, mapOf(TestTitleKey to "From override")))
    }

    @Test
    fun `resolveTitle falls back to the override when the key title is null`() {
        val spec = TopAppBarSpec()
        assertEquals("From override", spec.resolveTitle(TestTitleKey, mapOf(TestTitleKey to "From override")))
    }

    @Test
    fun `resolveTitle falls back to the override when the key title is blank`() {
        val spec = TopAppBarSpec(title = { "" })
        assertEquals("From override", spec.resolveTitle(TestTitleKey, mapOf(TestTitleKey to "From override")))
    }

    @Test
    fun `resolveTitle is empty without a key title and without an override`() {
        val spec = TopAppBarSpec(title = { "" })
        assertEquals("", spec.resolveTitle(TestTitleKey, emptyMap()))
    }

    @Test
    fun `resolveTitle only reads the override of the given key`() {
        val spec = TopAppBarSpec(title = { "" })
        assertEquals("", spec.resolveTitle(TestTitleKey, mapOf(TestOtherTitleKey to "From another key")))
    }
}
