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

package app.mobilitytechnologies.uitest.page

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import kotlin.reflect.KClass

/**
 * 特定のActivityを起動したり、
 * Activityが直接内包している[androidx.navigation.fragment.NavHostFragment]が管理しているFragmentを起動するための[Page]実装です。
 *
 * 利用例を以下に示します。
 * ```
 * @JvmField
 * @RegisterExtension
 * val uiTestExtension = UiTestExtension { SimpleActivityPage(it, MyActivity::class) }
 *
 * ...
 *
 * @Test
 * fun myTest() {
 *     val intent = (MyActivityを起動するためのIntent)
 *     // MyActivityを起動する
 *     uiTestExtension.page.launchActivitySimply(intent)
 *
 *     // スクリーンショットを撮る
 *     uiTestExtension.page.captureDisplay("起動直後")
 * }
 * ```
 *
 * @param A 起動するActivityの型を指定します
 * @param uiTestExtension [UiTestExtension]のインスタンスを指定します。
 * @param activityClass 起動するActivityを表すクラスオブジェクトを指定します。
 * @param viewIdSearchingNavController 起動するActivityが`NavHostFragment`を内包している場合、
 *   そのNavHostFragmentがセットされているview IDを指定します。
 *   [launchFragmentByNavController]を呼び出してFragmentを起動するときだけ意味を持ちます。
 */
class SimpleActivityPage<A : AppCompatActivity>(
        uiTestExtension: UiTestExtension<*>,
        activityClass: KClass<A>,
        @IdRes viewIdSearchingNavController: Int? = null
) : ActivityScenarioPage<SimpleActivityPage<A>, A>(uiTestExtension, activityClass, viewIdSearchingNavController) {
    override fun setUp(block: (SimpleActivityPage<A>.() -> Unit)?) {
        if (block != null) {
            this.block()
        }
    }

    override fun tearDown(block: (SimpleActivityPage<A>.() -> Unit)?) {
        if (block != null) {
            this.block()
        }
    }
}