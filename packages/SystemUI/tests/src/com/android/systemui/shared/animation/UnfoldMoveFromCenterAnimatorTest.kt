/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.shared.animation

import android.graphics.Point
import android.test.suitebuilder.annotation.SmallTest
import android.testing.AndroidTestingRunner
import android.view.Display
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_90
import android.view.View
import android.view.WindowManager
import com.android.systemui.SysuiTestCase
import com.android.systemui.util.mockito.any
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.junit.MockitoJUnit
import org.mockito.Mockito.`when` as whenever

@RunWith(AndroidTestingRunner::class)
@SmallTest
class UnfoldMoveFromCenterAnimatorTest : SysuiTestCase() {

    @Mock
    private lateinit var windowManager: WindowManager

    @get:Rule
    val mockito = MockitoJUnit.rule()

    private lateinit var animator: UnfoldMoveFromCenterAnimator

    @Before
    fun before() {
        animator = UnfoldMoveFromCenterAnimator(windowManager)
    }

    @Test
    fun testRegisterViewOnTheLeftOfVerticalFold_halfProgress_viewTranslatedToTheRight() {
        givenScreen(width = 100, height = 100, rotation = ROTATION_0)
        val view = createView(x = 20, width = 10, height = 10)
        animator.registerViewForAnimation(view)
        animator.onTransitionStarted()

        animator.onTransitionProgress(0.5f)

        // Positive translationX -> translated to the right
        // 10x10 view center is 25px from the center,
        // When progress is 0.5 it should be translated at:
        // 25 * 0.3 * (1 - 0.5) = 3.75px
        assertThat(view.translationX).isWithin(0.01f).of(3.75f)
    }

    @Test
    fun testRegisterViewOnTheLeftOfVerticalFold_zeroProgress_viewTranslatedToTheRight() {
        givenScreen(width = 100, height = 100, rotation = ROTATION_0)
        val view = createView(x = 20, width = 10, height = 10)
        animator.registerViewForAnimation(view)
        animator.onTransitionStarted()

        animator.onTransitionProgress(0f)

        // Positive translationX -> translated to the right
        // 10x10 view center is 25px from the center,
        // When progress is 0 it should be translated at:
        // 25 * 0.3 * (1 - 0) = 7.5px
        assertThat(view.translationX).isWithin(0.01f).of(7.5f)
    }

    @Test
    fun testRegisterViewOnTheLeftOfVerticalFold_fullProgress_viewTranslatedToTheOriginalPosition() {
        givenScreen(width = 100, height = 100, rotation = ROTATION_0)
        val view = createView(x = 20, width = 10, height = 10)
        animator.registerViewForAnimation(view)
        animator.onTransitionStarted()

        animator.onTransitionProgress(1f)

        // Positive translationX -> translated to the right
        // 10x10 view center is 25px from the center,
        // When progress is 1 it should be translated at:
        // 25 * 0.3 * 0 = 0px
        assertThat(view.translationX).isEqualTo(0f)
    }

    @Test
    fun testViewOnTheLeftOfVerticalFoldWithTranslation_halfProgress_viewTranslatedToTheRight() {
        givenScreen(width = 100, height = 100, rotation = ROTATION_0)
        val view = createView(x = 20, width = 10, height = 10, translationX = 100f)
        animator.registerViewForAnimation(view)
        animator.onTransitionStarted()

        animator.onTransitionProgress(0.5f)

        // Positive translationX -> translated to the right, original translation is ignored
        // 10x10 view center is 25px from the center,
        // When progress is 0.5 it should be translated at:
        // 25 * 0.3 * (1 - 0.5) = 3.75px
        assertThat(view.translationX).isWithin(0.01f).of(3.75f)
    }

    @Test
    fun testRegisterViewAndUnregister_halfProgress_viewIsNotUpdated() {
        givenScreen(width = 100, height = 100, rotation = ROTATION_0)
        val view = createView(x = 20, width = 10, height = 10)
        animator.registerViewForAnimation(view)
        animator.onTransitionStarted()
        animator.clearRegisteredViews()

        animator.onTransitionProgress(0.5f)

        assertThat(view.translationX).isEqualTo(0f)
    }

    @Test
    fun testRegisterViewUpdateProgressAndUnregister_halfProgress_viewIsNotUpdated() {
        givenScreen(width = 100, height = 100, rotation = ROTATION_0)
        val view = createView(x = 20, width = 10, height = 10)
        animator.registerViewForAnimation(view)
        animator.onTransitionStarted()
        animator.onTransitionProgress(0.2f)
        animator.clearRegisteredViews()

        animator.onTransitionProgress(0.5f)

        assertThat(view.translationX).isEqualTo(0f)
    }

    @Test
    fun testRegisterViewOnTheTopOfHorizontalFold_halfProgress_viewTranslatedToTheBottom() {
        givenScreen(width = 100, height = 100, rotation = ROTATION_90)
        val view = createView(y = 20, width = 10, height = 10)
        animator.registerViewForAnimation(view)
        animator.onTransitionStarted()

        animator.onTransitionProgress(0.5f)

        // Positive translationY -> translated to the bottom
        assertThat(view.translationY).isWithin(0.01f).of(3.75f)
    }

    private fun createView(
        x: Int = 0,
        y: Int = 0,
        width: Int = 10,
        height: Int = 10,
        translationX: Float = 0f,
        translationY: Float = 0f
    ): View {
        val view = spy(View(context))
        doAnswer {
            val location = (it.arguments[0] as IntArray)
            location[0] = x
            location[1] = y
            Unit
        }.`when`(view).getLocationOnScreen(any())

        whenever(view.width).thenReturn(width)
        whenever(view.height).thenReturn(height)

        return view.apply {
            setTranslationX(translationX)
            setTranslationY(translationY)
        }
    }

    private fun givenScreen(
        width: Int = 100,
        height: Int = 100,
        rotation: Int = ROTATION_0
    ) {
        val display = mock(Display::class.java)
        whenever(display.getSize(any())).thenAnswer {
            val size = (it.arguments[0] as Point)
            size.set(width, height)
            Unit
        }
        whenever(display.rotation).thenReturn(rotation)
        whenever(windowManager.defaultDisplay).thenReturn(display)

        animator.updateDisplayProperties()
    }
}
