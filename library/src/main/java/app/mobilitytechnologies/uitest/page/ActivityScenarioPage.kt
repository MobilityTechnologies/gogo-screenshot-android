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
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import app.mobilitytechnologies.uitest.espresso.monitorActivity
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.scenario.ActivityOrFragmentScenario
import app.mobilitytechnologies.uitest.snapshot.ActivityOrFragmentSnapShotTaker
import app.mobilitytechnologies.uitest.utils.toResourceEntryName
import com.google.android.gms.maps.SupportMapFragment
import kotlin.reflect.KClass

/**
 * [ActivityScenario]を使って画面を起動するPage実装です。
 *
 * @param IMPL 実装クラスの型
 * @param A ActivityScenarioを使って起動するActivity
 * @param uiTestExtension [UiTestExtension]のインスタンスを指定します。
 * @param activityClass 起動するActivityを表すクラスオブジェクトを指定します。
 * @param viewIdSearchingNavController 起動するActivityが`NavHostFragment`を内包している場合、
 *   そのNavHostFragmentがセットされているview IDを指定します。
 *   [launchFragmentByNavController]を呼び出してFragmentを起動するときだけ意味を持ちます。
 */
abstract class ActivityScenarioPage<IMPL, A : AppCompatActivity>(
        uiTestExtension: UiTestExtension<*>,
        val activityClass: KClass<A>,
        @IdRes val viewIdSearchingNavController: Int? = null
) : ActivityOrFragmentScenarioPage<IMPL, A, Nothing>(uiTestExtension) {

    val scenario: ActivityScenario<A>
        get() = (activityOrFragmentScenario as ActivityOrFragmentScenario.ActivityScenario).scenario

    // =================
    // 画面起動系のメソッド
    // =================
    /**
     * コンストラクタ呼び出し時に指定した引数[activityClass]のActivityを起動します。
     * Activityの起動に成功すると、対応する[ActivityScenario] (をラップしたオブジェクト) が[activityOrFragmentScenario]に格納されます。
     *
     * @param intent [activityClass]を起動するのに必要なIntentを指定します。
     */
    open fun launchActivitySimply(intent: Intent? = null) {
        if (snapShotPageName == null) {
            snapShotPageName = activityClass.java.simpleName
        }
        activityOrFragmentScenario = launchActivityWithMonitoring(intent)
    }

    /**
     * 起動したActivityが直接内包しているFragmentのうち、[F]型のものを取得し、それを[block]に渡して実行します。
     *
     * [launchFragmentByNavController]を使ってFragmentを起動した場合には使えません。
     *
     * @param fragmentId 取得したいFragmentのIDを指定します。
     *   起動したActivityが内包している[F]型のFragmentが1つだけの場合は指定不要です(先頭のFragmentを取得します)
     *   [fragmentTag]と両方が指定された場合は、この引数が優先されます。
     * @param fragmentTag 取得したいFragmentのTagを指定します。
     *   起動したActivityが内包している[F]型のFragmentが1つだけの場合は指定不要です(先頭のFragmentを取得します)
     *   この引数を指定するときは[fragmentId]は指定しないでください(nullを指定してください)。
     * @param F 取得したいFragmentの型を指定します。
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified F : Fragment> onFragment(
            @IdRes fragmentId: Int? = null,
            fragmentTag: String? = null,
            crossinline block: (F) -> Unit
    ) {
        this.scenario.onActivity {
            val fragmentManager = it.supportFragmentManager
            val fragment = when {
                fragmentId != null -> fragmentManager.findFragmentById(fragmentId)
                fragmentTag != null -> fragmentManager.findFragmentByTag(fragmentTag)
                else -> fragmentManager.fragments.filterIsInstance<F>().first()
            }
            block(fragment as F)
        }
    }

    /**
     * [onFragment]で得られるFragmentが直接内包している[SupportMapFragment]のヘルパーオブジェクトを取得します。
     * このヘルパーオブジェクトを使って地図の表示完了を待ち合わせることができます。
     *
     * [launchFragmentByNavController]を使ってFragmentを起動している場合は使えません。
     *
     * @param F [onFragment]で得られるFragmentの型を指定します。
     * @param fragmentId [onFragment]で得られるFragmentのIDを指定します。
     *   起動したActivityが内包している型[F]のFragmentが1つだけの場合は指定不要です
     *   (型[F]のFragmentのうち最初に見付かったものを取得します)。
     *   この引数がnon-nullの場合、[fragmentTag]は無視されます。
     * @param fragmentTag [onFragment]で得られるFragmentのTagを指定します。
     *   起動したActivityが内包している型[F]のFragmentが1つだけの場合は指定不要です
     *   (型[F]のFragmentのうち最初に見付かったものを取得します)。
     *   この引数を指定する場合は[fragmentId]は指定しないでください(`null`を指定してください)。
     * @param mapFragmentId 目的の[SupportMapFragment]に対応するfragment IDを指定します。
     *   [onFragment]で得られるFragmentが直接内包している`SupportMapFragment`が1つだけの場合は指定不要です。
     */
    inline fun <reified F : Fragment> getSupportMapFragmentHelper(
            @IdRes fragmentId: Int? = null,
            fragmentTag: String? = null,
            @IdRes mapFragmentId: Int? = null
    ) = SupportMapFragmentHelper(uiTestExtension, { this.onFragment<F>(fragmentId, fragmentTag, it) }, mapFragmentId)

    /**
     * コンストラクタ呼び出し時に指定した引数[activityClass]のホストActivityを起動してから、[NavController]を使って特定のFragmentを起動します。
     * 起動に成功すると、ホストActivity対応する[ActivityScenario] (をラップしたオブジェクト) が[activityOrFragmentScenario]に格納されます。
     *
     * [viewIdSearchingNavController]がnullの場合は、本メソッドを呼び出すことはできません。呼び出した場合は例外が発生します。
     *
     * @param fragmentDestination 起動したいFragmentのdestination IDを指定します。
     * @param intent 目的のFragmentの親Activityを起動するのに必要なIntentを指定します。
     * @param navigateAction 目的のFragmentを起動するために必要な処理を指定します。`null`が指定されている場合は何もしません。
     */
    open fun launchFragmentByNavController(@IdRes fragmentDestination: Int,
                                           intent: Intent? = null,
                                           navigateAction: ((NavController) -> Unit)? = null) {
        checkNotNull(viewIdSearchingNavController)
        // Fragment名を取る方法がわからないので、destination IDから画面名を生成する
        if (snapShotPageName == null) {
            val part1 = activityClass.java.simpleName
            val part2 = toResourceEntryName(fragmentDestination).capitalize()
            snapShotPageName = "${part1}-${part2}"
        }
        uiTestExtension.countingIdlingResource.increment()
        activityOrFragmentScenario = launchActivityWithMonitoring(intent).also { _scenario ->
            launchFragmentByNavControllerInternal(_scenario, fragmentDestination, viewIdSearchingNavController, navigateAction)
        }
    }

    /**
     * [activityClass]を起動し、[UiTestExtension.dataBindingIdlingResource]による監視対象にします。
     *
     * @param intent [activityClass]を起動するのに必要なIntentを指定します。
     *   指定する[intent]には、必ず[activityClass]が起動するようなものを指定してください。
     *   このメソッドでは[intent]が本当に[activityClass]を起動するものかどうかのチェックは行いません。
     * @return 起動した画面に対応する[ActivityScenario]のラッパーオブジェクトを返します。
     */
    protected fun launchActivityWithMonitoring(intent: Intent? = null) =
            if (intent != null) {
                ActivityScenario.launch(intent)
            } else {
                ActivityScenario.launch(activityClass.java)
            }.let {
                uiTestExtension.dataBindingIdlingResource.monitorActivity(it)
                ActivityOrFragmentScenario.ActivityScenario(it)
            }

    // ==========================
    // スクリーンショット系のメソッド
    // ==========================

    /**
     * Viewをキャプチャします。
     * Activityを受け取る[func]を呼び出した結果返されるViewをキャプチャします。
     *
     * @param condition キャプチャ時点の画面の状態を表す文字列を指定します。結果レポートに表示されます。
     * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
     *   `null`以外が指定された場合は結果レポートに表示されます。
     * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
     * @param func キャプチャ対象を返す関数を指定します。この関数の引数には、画面に表示されているActivityが渡されます。
     */
    open fun captureView(condition: String, optionalDescription: String?, waitUntilIdle: Boolean, func: (A) -> View) {
        captureViewFromActivityOrFragment(condition, optionalDescription, waitUntilIdle) {
            when (it) {
                is ActivityOrFragment.Activity -> func(it.activity)
                is ActivityOrFragment.Fragment -> throw IllegalStateException("Never reached")
            }
        }
    }

    /**
     * 1つのテストメソッド内で、同じ状態(条件)で画面・Viewを複数枚キャプチャします。
     * 次のように使います。
     *
     * ```
     * captureSequentially("XYZ状態") {
     *     captureDisplay() // 1枚目の撮影
     *     ...
     *     captureDisplay() // 2枚目の撮影
     *     ...
     * }
     * ```
     *
     * @param condition キャプチャする対象の状態(または条件)を表す文字列を指定します。
     *   結果レポートでは、このメソッドでキャプチャしたもの全てについて、この引数で指定した文字列が表示されます。
     * @param block [condition]で指定した条件下でキャプチャする処理を書くブロックです。
     *   キャプチャには [ActivityScenarioPage.SequentialSnapShotTaker]が提供するメソッドが利用できます。
     */
    fun captureSequentially(condition: String, block: SequentialSnapShotTaker<A>.() -> Unit) {
        val sequentialSnapShotTaker = SequentialSnapShotTaker(this, condition)
        sequentialSnapShotTaker.block()
    }

    /**
     * `(A) -> View`型の関数を指定できる [captureView] を提供する [ActivityOrFragmentSnapShotTaker.SequentialSnapShotTaker] 実装です。
     */
    class SequentialSnapShotTaker<A : AppCompatActivity>(
            private val activityScenarioPage: ActivityScenarioPage<*, A>,
            condition: String
    ) : ActivityOrFragmentSnapShotTaker.SequentialSnapShotTaker<A, Nothing>(activityScenarioPage, condition) {

        /**
         * Viewをキャプチャします。
         * [ActivityScenarioPage.captureSequentially]メソッドの引数ブロックの中で利用できる[ActivityScenarioPage.captureView]です。
         *
         * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
         *   `null`以外が指定された場合は結果レポートに表示されます。
         * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
         * @param func キャプチャ対象を返す関数を指定します。この関数の引数には、画面に表示されているActivityが渡されます。
         */
        fun captureView(optionalDescription: String?, waitUntilIdle: Boolean, func: (A) -> View) {
            activityScenarioPage.captureView(condition, optionalDescription, waitUntilIdle, func)
        }
    }
}