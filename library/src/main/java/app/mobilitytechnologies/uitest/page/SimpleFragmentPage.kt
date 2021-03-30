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
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.scenario.FragmentTestingActivity
import kotlin.reflect.KClass

/**
 * 特定のFragmentを起動したり、
 * そのFragmentが直接内包している[androidx.navigation.fragment.NavHostFragment]が管理しているFragmentを起動するための[Page]実装です。
 *
 * FragmentをホストするActivityには[FragmentTestingActivity]が使われます。
 *
 * 利用例を以下に示します。
 *
 * ```
 * @JvmField
 * @RegisterExtension
 * val uiTestExtension = UiTestExtension { SimpleFragmentPage(it, MyFragment::class) }
 *
 * ...
 *
 * @Test
 * fun myTest() {
 *     // MyFragmentを起動する
 *     uiTestExtension.page.launchFragmentSimply()
 *
 *     // スクリーンショットを撮る
 *     uiTestExtension.page.captureDisplay("起動直後")
 * }
 * ```
 *
 * @param F 起動するFragmentの型を指定します
 * @param uiTestExtension [UiTestExtension]のインスタンスを指定します
 * @param fragmentClass 起動するFragmentを表すクラスオブジェクトを指定します
 * @param themeResId ホストActivityに適用したいテーマを指定します
 * @param contentLayoutId 起動するFragmentが`NavHostFragment`を内包している場合、そのNavHostFragmentがセットされているview IDを指定します。
 *   [launchChildFragmentByNavController]を呼び出してFragmentを起動するときだけ意味を持ちます。
 */
class SimpleFragmentPage<F : Fragment>(
        uiTestExtension: UiTestExtension<*>,
        fragmentClass: KClass<F>,
        @StyleRes themeResId: Int = androidx.appcompat.R.style.Theme_AppCompat,
        @IdRes contentLayoutId: Int? = null
) : FragmentScenarioPage<SimpleFragmentPage<F>, F, FragmentTestingActivity>(
        uiTestExtension = uiTestExtension,
        fragmentClass = fragmentClass,
        hostActivityClass = FragmentTestingActivity::class,
        hostActivityIntent = FragmentTestingActivity.createIntent(ApplicationProvider.getApplicationContext(), themeResId),
        contentLayoutId = contentLayoutId) {

    override fun setUp(block: (SimpleFragmentPage<F>.() -> Unit)?) {
        if (block != null) {
            this.block()
        }
    }

    override fun tearDown(block: (SimpleFragmentPage<F>.() -> Unit)?) {
        if (block != null) {
            this.block()
        }
    }
}