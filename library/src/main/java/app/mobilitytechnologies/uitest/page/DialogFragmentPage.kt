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

import android.content.Intent
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.scenario.FragmentTestingActivity
import app.mobilitytechnologies.uitest.scenario.TestingFragmentHost
import com.google.android.gms.maps.SupportMapFragment
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

/**
 * ダイアログフラグメントを起動するまで待つような[Page]実装です。
 *
 * [FragmentScenarioPage]を使って[dialogHostingFragmentClass]を起動し、
 * それをホストとして指定されたダイアログフラグメントを表示します。
 *
 * 利用例はREADME.mdを参照してください。
 *
 * [captureActivityOrFragment]メソッドではダイアログの表示内容をキャプチャできません。用途に応じて次のいずれかのメソッド使ってください。
 * - 画面全体(スクリーン全体)をキャプチャしたいとき: [captureDisplay]
 * - ダイアログ内に表示されている内容だけをキャプチャしたいとき: [captureDialogFragment]
 *
 * @param uiTestExtension `RegisterExtension`で登録したUiTestExtensionのインスタンスを指定します。
 * @param dialogFragmentClass 起動したいダイアログフラグメントのクラスを指定します。
 * @param dialogHostingFragmentClass ダイアログフラグメントをホストするフラグメントのクラスを指定します。
 *   起動したいダイアログフラグメントが特にホスト側にリスナインターフェイスの実装を要求していない場合は
 *   [DialogHostingFragment]を指定してください。
 *   特定のリスナインターフェイスの実装を要求する場合はその要求を満たすクラスを指定してください。詳細はGitHub.comのWikiを参照してください。
 * @param hostActivityClass 起動するFragmentをホストするActivityを指定します。
 * @param hostActivityIntent [hostActivityClass]を起動するためのIntentを指定します。
 */
abstract class DialogFragmentPage<IMPL, D : DialogFragment, DH : DialogHostingFragment, HA>(
        uiTestExtension: UiTestExtension<*>,
        val dialogFragmentClass: KClass<D>,
        @Suppress("UNCHECKED_CAST")
        val dialogHostingFragmentClass: KClass<DH> = DialogHostingFragment::class as KClass<DH>,
        @Suppress("UNCHECKED_CAST")
        hostActivityClass: KClass<HA> = FragmentTestingActivity::class as KClass<HA>,
        hostActivityIntent: Intent = FragmentTestingActivity.createIntent(ApplicationProvider.getApplicationContext())
) : FragmentScenarioPage<IMPL, DH, HA>(uiTestExtension, dialogHostingFragmentClass, hostActivityClass, hostActivityIntent)
        where HA : AppCompatActivity, HA : TestingFragmentHost {

    companion object {
        const val TAG_DIALOG_FRAGMENT = "app.mobilitytechnologies.uitest.page.DialogFragmentPage_DialogFragment_Tag"
    }

    // TODO: launchDialogFragment(fragmentArgs: Bundle? = null, factory: FragmentFactory? = null) は必要になり次第実装する。

    /**
     * ダイアログフラグメントを表示します。
     * 表示するダイアログフラグメントは、引数[dialogCreator]を呼び出すことによってインスタンス化します。
     *
     * @param dialogCreator 表示したいダイアログフラグメントをインスタンス化する処理が書かれたλ式を指定します。
     * @throws IllegalArgumentException [dialogHostingFragmentClass]にコンストラクタが無いとき
     * @throws UnsupportedOperationException [dialogHostingFragmentClass]コンストラクタ引数にKotlinで表現できない型が宣言されているとき (おそらく発生することはない)
     * @throws IllegalArgumentException [dialogHostingFragmentClass]コンストタクタのいずれかの引数の型がインターフェイスではないとき
     */
    open fun launchDialogFragmentByCreator(dialogCreator: () -> DialogFragment) {
        uiTestExtension.countingIdlingResource.increment()
        if (snapShotPageName == null) {
            snapShotPageName = dialogFragmentClass.java.simpleName
        }
        launchFragmentByCreator { newDialogHostingFragment(dialogHostingFragmentClass) }

        scenario.onFragment {
            it.showDialogFragment(dialogCreator(), TAG_DIALOG_FRAGMENT)
            uiTestExtension.countingIdlingResource.decrement()
        }
        Espresso.onIdle()
    }

    /**
     * 起動したダイアログフラグメントを引数に[block]を呼び出します。
     */
    @Suppress("UNCHECKED_CAST")
    fun onDialogFragment(block: (D) -> Unit) {
        scenario.onFragment {
            val dialogFragment = it.childFragmentManager.findFragmentByTag(TAG_DIALOG_FRAGMENT)
            block(dialogFragment as D)
        }
    }

    /**
     * このダイアログが内包している[SupportMapFragment]のヘルパーオブジェクトを取得します。
     * このヘルパーオブジェクトを使って地図の表示完了を待ち合わせることができます。
     *
     * @param mapFragmentId 目的の[SupportMapFragment]に対応するfragment IDを指定します。
     *   このダイアログが内包している`SupportMapFragment`が1つだけの場合は指定不要です。
     */
    fun getSupportMapFragmentHelperOfDialogFragment(@IdRes mapFragmentId: Int? = null) =
            SupportMapFragmentHelper(uiTestExtension, this::onDialogFragment, mapFragmentId)

    /**
     * このダイアログ内に表示されている内容全体(画面全体ではない)をキャプチャします。
     * 画面全体をキャプチャしたいときは[captureDisplay]を使ってください。
     *
     * @param condition キャプチャ時点の画面の状態を表す文字列を指定します。結果レポートに表示されます。
     * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
     *   `null`以外が指定された場合は結果レポートに表示されます。
     * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
     */
    fun captureDialogFragment(condition: String, optionalDescription: String? = null, waitUntilIdle: Boolean = true) {
        if (waitUntilIdle) {
            Espresso.onIdle()
        }
        val snapShotFileName = snapShotNameCreator.createFileName(
                snapShotPageName!!, condition, snapShotCounter.getAndIncrement(), optionalDescription)
        onDialogFragment {
            snapShot.capture(it, snapShotFileName)
        }
    }

    /**
     * 引数[dialogHostingFragmentClass]をインスタンス化します。
     * それぞれのリスナインターフェースを空実装したオブジェクトをコンストラクタの引数に指定してインスタンス化します。
     *
     * @throws IllegalArgumentException [dialogHostingFragmentClass]にコンストラクタが無いとき
     * @throws UnsupportedOperationException [dialogHostingFragmentClass]コンストラクタ引数にKotlinで表現できない型が宣言されているとき (おそらく発生することはない)
     * @throws IllegalArgumentException [dialogHostingFragmentClass]コンストタクタのいずれかの引数の型がインターフェイスではないとき
     */
    // `constructors`, `parameters`, `type`, `call`いずれもkotlin-reflect.jarが無くても呼べるのだが、何故か警告が出てしまうので抑止。
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    fun <DH : DialogHostingFragment> newDialogHostingFragment(dialogHostingFragmentClass: KClass<DH>): DH {
        // 厳密にはこれでprimary constructorが取れる保証はない。
        // とはいえ、テストコード上で宣言するDialogHostingFragmentのサブクラスで変なコンストラクタを宣言するケースも考えられないので、これで妥協する。
        // 厳密にやるなら、kotlin-reflectで定義されている`KClass<T>.primaryConstructor`を使うべき。
        val primaryCons = dialogHostingFragmentClass.constructors.firstOrNull()
                ?: throw IllegalArgumentException("Constructors not found: $dialogHostingFragmentClass")
        val paramTypeList = primaryCons.parameters.map {
            val paramType = (it.type.classifier as? KClass<*>)
                    ?: throw UnsupportedOperationException("The constructor of $dialogHostingFragmentClass requires unsupported argument: ${it.type}")
            paramType.takeIf { p -> p.java.isInterface }
                    ?: throw IllegalArgumentException("The constructor of $dialogHostingFragmentClass requires non-interface arguments: ${it.type}")
        }
        val consArgs: List<Any> = paramTypeList.map { newEmptyListener(it) }
        return primaryCons.call(*consArgs.toTypedArray())
    }

    /**
     * 引数[listenerInterface]を実装した、何もしないクラスのインスタンスを生成します。
     * このインスタンスのどのメソッドを呼び出しても、何もしません。
     *
     * 現実装では、戻り値の無い(voidやUnit型の)メソッドのみに対応しています。
     * 戻り値のあるようなメソッドが定義されたインターフェイスが渡されたときは、
     * 当該メソッドが呼び出されたタイミングで例外が発生します。
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> newEmptyListener(listenerInterface: KClass<T>): T =
            Proxy.newProxyInstance(listenerInterface.java.classLoader, arrayOf(listenerInterface.java)) { _, method, _ ->
                when (method.returnType) {
                    Void.TYPE -> Unit
                    Unit::class.java -> Unit
                    else -> throw UnsupportedOperationException("not supported: ${method.name}: ${method.returnType}")
                }
            } as T
}