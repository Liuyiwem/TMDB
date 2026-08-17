package com.yiwenliu.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private object TestFirstTopLevelKey : NavKey

private object TestSecondTopLevelKey : NavKey

private object TestThirdTopLevelKey : NavKey

private object TestKeyFirst : NavKey

private object TestKeySecond : NavKey

class NavigatorTest {
    private lateinit var navigationState: NavigationState
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        val startKey = TestFirstTopLevelKey
        val topLevelStack = NavBackStack<NavKey>(startKey)
        val topLevelKeys =
            listOf(
                startKey,
                TestSecondTopLevelKey,
                TestThirdTopLevelKey,
            )
        val subStacks = topLevelKeys.associateWith { key -> NavBackStack(key) }
        navigationState =
            NavigationState(
                startKey = startKey,
                topLevelStack = topLevelStack,
                subStacks = subStacks,
            )
        navigator = Navigator(navigationState)
    }

    @Test
    fun `startKey is the initial top-level key`() {
        assertEquals(TestFirstTopLevelKey, navigationState.startKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `navigate pushes key to sub-stack`() {
        navigator.navigate(TestKeyFirst)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
        assertEquals(TestKeyFirst, navigationState.subStacks[TestFirstTopLevelKey]?.last())
    }

    @Test
    fun `navigate switches top-level`() {
        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(TestSecondTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `navigate to same key is single-top`() {
        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
    }

    @Test
    fun `navigate to active top-level resets its stack`() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestSecondTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(
            listOf(TestSecondTopLevelKey),
            navigationState.currentSubStack.toList(),
        )
    }

    @Test
    fun `navigate builds the sub-stack`() {
        navigator.navigate(TestKeyFirst)
        assertEquals(TestKeyFirst, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
        navigator.navigate(TestKeySecond)
        assertEquals(TestKeySecond, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `each top-level keeps its own sub-stack`() {
        navigator.navigate(TestKeyFirst)
        assertEquals(TestKeyFirst, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(TestSecondTopLevelKey, navigationState.currentKey)
        assertEquals(TestSecondTopLevelKey, navigationState.currentTopLevelKey)
        navigator.navigate(TestKeySecond)
        assertEquals(TestKeySecond, navigationState.currentKey)
        assertEquals(TestSecondTopLevelKey, navigationState.currentTopLevelKey)
        navigator.navigate(TestFirstTopLevelKey)
        assertEquals(TestKeyFirst, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `goBack pops one sub key`() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestKeySecond)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst, TestKeySecond),
            navigationState.currentSubStack.toList(),
        )
        navigator.goBack()
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestKeyFirst, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `goBack from root returns to previous top-level`() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(
            listOf(TestSecondTopLevelKey),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestSecondTopLevelKey, navigationState.currentKey)
        assertEquals(TestSecondTopLevelKey, navigationState.currentTopLevelKey)
        navigator.goBack()
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestKeyFirst, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `goBack pops to sub-stack root`() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestKeySecond)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeyFirst, TestKeySecond),
            navigationState.currentSubStack.toList(),
        )
        navigator.goBack()
        navigator.goBack()
        assertEquals(
            listOf(TestFirstTopLevelKey),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestFirstTopLevelKey, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `goBack unwinds across top-levels`() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestSecondTopLevelKey, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
        navigator.navigate(TestThirdTopLevelKey)
        navigator.navigate(TestKeySecond)
        assertEquals(
            listOf(TestThirdTopLevelKey, TestKeySecond),
            navigationState.currentSubStack.toList(),
        )
        repeat(4) {
            navigator.goBack()
        }
        assertEquals(
            listOf(TestFirstTopLevelKey),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestFirstTopLevelKey, navigationState.currentKey)
        assertEquals(TestFirstTopLevelKey, navigationState.currentTopLevelKey)
    }

    @Test
    fun `goBack on empty stack throws`() {
        assertFailsWith<IllegalStateException> {
            navigator.goBack()
        }
    }

    @Test
    fun `navigating to a key already in the stack moves it to the top`() {
        navigator.navigate(TestKeyFirst)
        navigator.navigate(TestKeySecond)
        navigator.navigate(TestKeyFirst)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeySecond, TestKeyFirst),
            navigationState.currentSubStack.toList(),
        )
    }

    @Test
    fun `navigating to the start top-level clears the whole top-level history`() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestThirdTopLevelKey)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestSecondTopLevelKey, TestThirdTopLevelKey),
            navigationState.topLevelStack.toList(),
        )
        navigator.navigate(TestFirstTopLevelKey)
        assertEquals(listOf(TestFirstTopLevelKey), navigationState.topLevelStack.toList())
        assertFailsWith<IllegalStateException> { navigator.goBack() }
    }

    @Test
    fun `navigating to a non-start top-level reorders without clearing`() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestThirdTopLevelKey)
        navigator.navigate(TestSecondTopLevelKey)
        assertEquals(
            listOf(TestFirstTopLevelKey, TestThirdTopLevelKey, TestSecondTopLevelKey),
            navigationState.topLevelStack.toList(),
        )
    }

    @Test
    fun `canGoBack is false only at the start route`() {
        assertFalse(navigator.canGoBack)
        navigator.navigate(TestKeyFirst)
        assertTrue(navigator.canGoBack)
        navigator.goBack()
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `replaceStack discards the previous sub-stack of the target top-level`() {
        navigator.navigate(TestKeyFirst)
        navigator.replaceStack(listOf(TestFirstTopLevelKey, TestKeySecond))
        assertEquals(
            listOf(TestFirstTopLevelKey, TestKeySecond),
            navigationState.currentSubStack.toList(),
        )
        assertEquals(TestKeySecond, navigationState.currentKey)
    }

    @Test
    fun `replaceStack makes the first key the current top-level`() {
        navigator.replaceStack(listOf(TestSecondTopLevelKey, TestKeyFirst))
        assertEquals(TestSecondTopLevelKey, navigationState.currentTopLevelKey)
        assertEquals(TestKeyFirst, navigationState.currentKey)
    }

    @Test
    fun `replaceStack to the start top-level resets the top-level history`() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestThirdTopLevelKey)
        navigator.replaceStack(listOf(TestFirstTopLevelKey, TestKeyFirst))
        assertEquals(listOf(TestFirstTopLevelKey), navigationState.topLevelStack.toList())
    }

    @Test
    fun `replaceStack to a non-start top-level keeps the start key below it`() {
        navigator.navigate(TestThirdTopLevelKey)
        navigator.replaceStack(listOf(TestSecondTopLevelKey, TestKeyFirst))
        assertEquals(
            listOf(TestFirstTopLevelKey, TestSecondTopLevelKey),
            navigationState.topLevelStack.toList(),
        )
    }

    @Test
    fun `replaceStack to a non-start top-level resets the start sub-stack`() {
        navigator.navigate(TestKeyFirst)
        navigator.replaceStack(listOf(TestSecondTopLevelKey, TestKeySecond))
        assertEquals(
            listOf(TestFirstTopLevelKey),
            navigationState.subStacks[TestFirstTopLevelKey]?.toList(),
        )
    }

    @Test
    fun `replaceStack leaves the other sub-stacks untouched`() {
        navigator.navigate(TestSecondTopLevelKey)
        navigator.navigate(TestKeyFirst)
        navigator.replaceStack(listOf(TestFirstTopLevelKey, TestKeySecond))
        assertEquals(
            listOf(TestSecondTopLevelKey, TestKeyFirst),
            navigationState.subStacks[TestSecondTopLevelKey]?.toList(),
        )
    }

    @Test
    fun `goBack after replaceStack returns to the top-level root`() {
        navigator.replaceStack(listOf(TestFirstTopLevelKey, TestKeySecond))
        navigator.goBack()
        assertEquals(listOf(TestFirstTopLevelKey), navigationState.currentSubStack.toList())
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `goBack after replaceStack to a non-start top-level unwinds to the start key`() {
        navigator.replaceStack(listOf(TestSecondTopLevelKey, TestKeyFirst))
        navigator.goBack()
        navigator.goBack()
        assertEquals(TestFirstTopLevelKey, navigationState.currentKey)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `replaceStack with only the top-level key cannot go back`() {
        navigator.navigate(TestKeyFirst)
        navigator.replaceStack(listOf(TestFirstTopLevelKey))
        assertFalse(navigator.canGoBack)
        assertFailsWith<IllegalStateException> { navigator.goBack() }
    }

    @Test
    fun `replaceStack is idempotent`() {
        val stack = listOf(TestFirstTopLevelKey, TestKeyFirst)
        navigator.replaceStack(stack)
        navigator.replaceStack(stack)
        assertEquals(stack, navigationState.currentSubStack.toList())
        assertEquals(listOf(TestFirstTopLevelKey), navigationState.topLevelStack.toList())
    }

    @Test
    fun `replaceStack with an empty stack throws`() {
        assertFailsWith<IllegalArgumentException> { navigator.replaceStack(emptyList()) }
    }

    @Test
    fun `replaceStack with a non top-level first key throws`() {
        assertFailsWith<IllegalArgumentException> { navigator.replaceStack(listOf(TestKeyFirst)) }
    }
}
