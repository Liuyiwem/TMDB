package com.yiwenliu.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

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
}
