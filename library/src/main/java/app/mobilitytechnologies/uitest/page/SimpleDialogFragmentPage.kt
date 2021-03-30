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

import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ApplicationProvider
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.scenario.FragmentTestingActivity
import kotlin.reflect.KClass

/**
 * DialogFragmentを起動するための[Page]実装です。
 *
 * [FragmentTestingActivity]を起動し、その内部に[dialogHostingFragmentClass]を埋め込みます。
 * その`dialogHostingFragmentClass`をホストとして、指定された`DialogFragment`を表示します。
 *
 * 利用例を以下に示します。
 *
 * ```
 * @JvmField
 * @RegisterExtension
 * val uiTestExtension = UiTestExtension {
 *     SimpleDialogFragmentPage(it, MyDialogFragment::class, DialogHostingFragment::class)
 * }
 *
 * ...
 *
 * @Test
 * fun myTest() {
 *     // MyDialogFragmentを起動する
 *     uiTestExtension.page.launchDialogFragmentByCreator {
 *         MyDialogFragment.newInstance(....)
 *     }
 *
 *     // スクリーンショットを撮る
 *     uiTestExtension.page.captureDisplay("起動直後")
 * }
 * ```
 * @param D 起動したい[DialogFragment]の型を指定します
 * @param DH `DialogFragment`をホストするFragmentの型を指定します。
 *   [DialogHostingFragment]のサブタイプである必要があります
 * @param uiTestExtension `RegisterExtension`で登録したUiTestExtensionのインスタンスを指定します。
 * @param dialogFragmentClass 起動したい`DialogFragment`のクラスを指定します。
 * @param dialogHostingFragmentClass [dialogFragmentClass]をホストするFragmentのクラスを指定します。
 *   [dialogFragmentClass]がホスト側にリスナインターフェイスの実装を要求していない場合は
 *   [DialogHostingFragment]を指定してください。
 * @param themeResId [dialogHostingFragmentClass]をホストするActivityに適用するテーマを指定します
 */
class SimpleDialogFragmentPage<D : DialogFragment, DH : DialogHostingFragment>(
        uiTestExtension: UiTestExtension<*>,
        dialogFragmentClass: KClass<D>,
        dialogHostingFragmentClass: KClass<DH>,
        @StyleRes themeResId: Int = androidx.appcompat.R.style.Theme_AppCompat
) : DialogFragmentPage<SimpleDialogFragmentPage<D, DH>, D, DH, FragmentTestingActivity>(
        uiTestExtension = uiTestExtension,
        dialogFragmentClass = dialogFragmentClass,
        dialogHostingFragmentClass = dialogHostingFragmentClass,
        hostActivityClass = FragmentTestingActivity::class,
        hostActivityIntent = FragmentTestingActivity.createIntent(ApplicationProvider.getApplicationContext(), themeResId)
) {
    override fun setUp(block: (SimpleDialogFragmentPage<D, DH>.() -> Unit)?) {
        if (block != null) {
            this.block()
        }
    }

    override fun tearDown(block: (SimpleDialogFragmentPage<D, DH>.() -> Unit)?) {
        if (block != null) {
            this.block()
        }
    }
}