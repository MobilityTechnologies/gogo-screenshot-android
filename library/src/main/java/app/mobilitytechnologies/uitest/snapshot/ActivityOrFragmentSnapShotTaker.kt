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

package app.mobilitytechnologies.uitest.snapshot

import android.os.SystemClock
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.repeatedlyUntil
import androidx.test.espresso.matcher.ViewMatchers.withId
import app.mobilitytechnologies.uitest.espresso.disableVerticalScrollBar
import app.mobilitytechnologies.uitest.espresso.doFirst
import app.mobilitytechnologies.uitest.espresso.isBelowBottomLine
import app.mobilitytechnologies.uitest.espresso.scrollVertically
import app.mobilitytechnologies.uitest.espresso.withViewAssertion

/**
 * ActivityまたはFragmentが表示されている画面やViewをキャプチャする機能を提供するインターフェイスです。
 */
interface ActivityOrFragmentSnapShotTaker<A : AppCompatActivity, F : Fragment> {
    /**
     * 画面全体をキャプチャします。
     * @param condition キャプチャ時点の画面の状態を表す文字列を指定します。結果レポートに表示されます。
     * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
     *   `null`以外が指定された場合は結果レポートに表示されます。
     * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
     */
    fun captureDisplay(condition: String, optionalDescription: String? = null, waitUntilIdle: Boolean = true)

    /**
     * Activity全体、またはFragment全体をキャプチャします。
     * @param condition キャプチャ時点の画面の状態を表す文字列を指定します。結果レポートに表示されます。
     * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
     *   `null`以外が指定された場合は結果レポートに表示されます。
     * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
     */
    fun captureActivityOrFragment(condition: String, optionalDescription: String? = null, waitUntilIdle: Boolean = true)

    /**
     * 引数で指定された`ScrollView`が初めて画面に表示されてから、スクロールバーが消えるまでの時間だけEspressoをbusy状態にし、
     * テストの進行を止めます。
     */
    fun ensureInitialScrollBarFadedAway(@IdRes scrollViewId: Int)

    /**
     * 同一状態で複数枚の画面/Viewをキャプチャするためのクラスです。
     * 状態を表す文字列はコンストラクに指定してください。
     */
    abstract class SequentialSnapShotTaker<A : AppCompatActivity, F : Fragment>
    (
            /**
             * 実際にキャプチャを行う[ActivityOrFragmentSnapShotTaker]インターフェイスの実装クラスのインスタンスを指定します。
             */
            private val snapShotTaker: ActivityOrFragmentSnapShotTaker<A, F>,

            /**
             * キャプチャ時点の状態を表す文字列を指定します。
             * このクラスの`capture`メソッドを呼ぶと、ここで指定した値が結果レポートに表示されるようになります。
             */
            protected val condition: String
    ) {
        /**
         * 画面全体をキャプチャします。
         * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
         *   `null`以外が指定された場合は結果レポートに表示されます。
         * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
         */
        fun captureDisplay(optionalDescription: String? = null, waitUntilIdle: Boolean = true) {
            snapShotTaker.captureDisplay(condition, optionalDescription, waitUntilIdle)
        }

        /**
         * Activity全体、またはFragment全体をキャプチャします。
         * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
         *   `null`以外が指定された場合は結果レポートに表示されます。
         * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
         */
        fun captureActivityOrFragment(optionalDescription: String? = null, waitUntilIdle: Boolean = true) {
            snapShotTaker.captureActivityOrFragment(condition, optionalDescription, waitUntilIdle)
        }

        /**
         * 引数[scrollViewId]で指定されたScrollViewについて、キャプチャしながら一番下までスクロールします。
         *
         * @param scrollViewId スクロールしたいScrollViewのリソースIDを指定します。
         * @param bottomViewId スクロール対象のScrollViewの中にあるViewのうち、一番下にあるViewのリソースIDを指定します。
         * ScrollViewの下端が、ここで指定されたリソースIDのViewの下端と一致するか、それより下に位置するまでスクロールとキャプチャを繰り返します。
         * @param maxAttempts スクロール操作の、最大試行回数を指定します。
         * ここで指定された回数スクロールしても、ScrollViewの下端が[bottomViewId]の下端と同じか下に位置しない場合は例外が発生します。
         * @param waitInitialScrollBarFadeAway スクロール開始前に [ensureInitialScrollBarFadedAway] を呼び出すかどうかを指定します。
         * ある画面(ActivityやFragment)で複数回このメソッドを呼ぶ場合、2回目以降は`false`を指定すると、テスト実行時間の速度向上が期待できます。
         * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
         *   `null`以外が指定された場合は結果レポートに表示されます。
         */
        fun captureEachScrolling(
                @IdRes scrollViewId: Int,
                @IdRes bottomViewId: Int,
                maxAttempts: Int = 5,
                waitInitialScrollBarFadeAway: Boolean = true,
                optionalDescription: String? = null
        ) {
            if (waitInitialScrollBarFadeAway) {
                snapShotTaker.ensureInitialScrollBarFadedAway(scrollViewId)
            }
            onView(withId(scrollViewId))
                    .perform(
                            disableVerticalScrollBar(),
                            repeatedlyUntil(
                                    scrollVertically().doFirst {
                                        captureDisplay(optionalDescription = optionalDescription, waitUntilIdle = false)
                                    },
                                    withViewAssertion(isBelowBottomLine(withId(bottomViewId))), maxAttempts))
            // 端までスクロールしたときの反動の影がスクショに映りこんでしまうので500ms待つ。
            // 固定値スリープは望ましくないが、他に良い方法が思い付かないので、一旦これで凌ぐ。
            SystemClock.sleep(500)
            captureDisplay(optionalDescription = optionalDescription)
        }
    }
}