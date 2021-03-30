/*
 * Copyright 2021 Mobility Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.mobilitytechnologies.uitest.espresso

import android.view.View
import android.widget.NumberPicker
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers
import kotlin.math.roundToInt

// TODO: RecyclerViewとかにも適用できるはずだが、一旦ScrollView, NestedScrollViewだけにしておく。
private fun getScrollViewMatcher() = CoreMatchers.anyOf(
        ViewMatchers.isAssignableFrom(ScrollView::class.java),
        ViewMatchers.isAssignableFrom(NestedScrollView::class.java)
)

/**
 * スクロールバーがフェードアウトするまでの時間待ちます。
 * 具体的には以下の2つの値を足した時間だけ(Espressoの)ビジー状態にします。
 * - [View.getScrollBarDefaultDelayBeforeFade]の4倍
 * - [View.getScrollBarFadeDuration]
 */
fun waitUntilScrollBarFadedAway(): ViewAction = ViewActions.actionWithAssertions(
        object : ViewAction {
            override fun getDescription() = "wait until the initial scroll bar faded away"

            override fun getConstraints() = getScrollViewMatcher()

            override fun perform(uiController: UiController, scrollView: View) {
                // View#initialAwakenScrollBars の実装を見ると、
                // 初回だけ scrollBarDefaultDelayBeforeFade の4倍待ってからフェードを開始している。
                val initialDelayBeforeFade = scrollView.scrollBarDefaultDelayBeforeFade * 4L
                val fadeDuration = scrollView.scrollBarFadeDuration
                val timeAmount = initialDelayBeforeFade + fadeDuration
                uiController.loopMainThreadForAtLeast(timeAmount)
            }
        }
)

fun disableVerticalScrollBar(): ViewAction = ViewActions.actionWithAssertions(
        object : ViewAction {
            override fun getConstraints() = getScrollViewMatcher()

            override fun getDescription(): String = "disable vertical scroll bar"

            override fun perform(uiController: UiController, view: View) {
                view.isVerticalScrollBarEnabled = false
                view.invalidate()
                uiController.loopMainThreadUntilIdle()
            }
        }
)

/**
 * このScrollViewの中身を縦スクロールするような[ViewAction]です。
 * 引数には、このScrollViewの高さに対する割合を指定してください。
 * デフォルトでは、ScrollViewの高さの0.8倍の量だけスクロールします。
 */
fun scrollVertically(amountRatio: Float = 0.8f): ViewAction = ViewActions.actionWithAssertions(
        object : ViewAction {
            override fun getConstraints() = getScrollViewMatcher()

            override fun getDescription(): String = "scroll vertically"

            override fun perform(uiController: UiController, view: View) {
                val amount: Float = view.height * amountRatio
                view.scrollBy(0, amount.roundToInt())
                // scrollByは非同期にinvalidate()しているので、ここで明示的にinvalidate()を呼んでおく必要がある。
                // さもないと、直後にスクリーンショットを撮ったときにスクロール前の画面になることがある。
                view.invalidate()
                uiController.loopMainThreadUntilIdle()
            }
        }
)

/**
 * 座標 (0,0) にScrollする
 */
fun scrollToTopLeft(): ViewAction = ViewActions.actionWithAssertions(
        object : ViewAction {
            override fun getConstraints() = getScrollViewMatcher()

            override fun getDescription(): String = "scroll to top left"

            override fun perform(uiController: UiController, view: View) {
                view.scrollTo(0, 0)
                // scrollToは非同期にinvalidate()しているので、ここで明示的にinvalidate()を呼んでおく必要がある。
                // さもないと、直後にスクリーンショットを撮ったときにスクロール前の画面になることがある。
                view.invalidate()
                uiController.loopMainThreadUntilIdle()
            }
        }
)

/**
 * 短い量だけ上にスワイプするような[ViewAction]です。
 */
fun swipeUpShort(): ViewAction = ViewActions.actionWithAssertions(
        GeneralSwipeAction(
                Swipe.FAST,
                TranslatedCoordinatesProvider(GeneralLocation.BOTTOM_CENTER, 0f, -0.083f),
                TranslatedCoordinatesProvider(GeneralLocation.TOP_CENTER, 0f, 0.6f),
                Press.FINGER
        )
)

/**
 * NumberPickerに対して値をセットするViewActionです。
 */
fun setNumberPickerValue(number: Int) = object : ViewAction {
    override fun getDescription() = "set number"

    override fun getConstraints() = CoreMatchers.allOf(ViewMatchers.isAssignableFrom(NumberPicker::class.java), ViewMatchers.isDisplayed())

    override fun perform(uiController: UiController, view: View) {
        val numberPicker = view as NumberPicker // always success
        numberPicker.value = number
    }
}

/**
 * 引数[hook]を実行してから、レシーバーとして指定された[ViewAction]を実行するような[ViewAction]を生成して返します。
 */
fun ViewAction.doFirst(hook: () -> Unit) = object : ViewAction {
    override fun getDescription() = this@doFirst.description

    override fun getConstraints() = this@doFirst.constraints

    override fun perform(uiController: UiController, view: View) {
        hook()
        this@doFirst.perform(uiController, view)
    }
}