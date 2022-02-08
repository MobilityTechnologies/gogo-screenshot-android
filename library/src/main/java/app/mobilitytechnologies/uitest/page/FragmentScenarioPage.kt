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
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.scenario.ActivityOrFragmentScenario
import app.mobilitytechnologies.uitest.scenario.AppCompatFragmentScenario
import app.mobilitytechnologies.uitest.scenario.FragmentTestingActivity
import app.mobilitytechnologies.uitest.scenario.TestingFragmentHost
import app.mobilitytechnologies.uitest.scenario.monitorFragment
import app.mobilitytechnologies.uitest.snapshot.ActivityOrFragmentSnapShotTaker
import com.google.android.gms.maps.SupportMapFragment
import kotlin.reflect.KClass

/**
 * [AppCompatFragmentScenario]を使って画面を起動するPage実装です。
 * @param IMPL 実装クラスの型
 * @param F FragmentScenarioを使って起動するFragmentの型
 * @param HA 起動するFragmentをホストするActivityの型
 * @param uiTestExtension [UiTestExtension]のインスタンスを指定します。
 * @param fragmentClass 起動するFragmentを表すクラスオブジェクトを指定します。
 * @param hostActivityClass 起動するFragmentをホストするActivityを指定します。
 * @param hostActivityIntent [hostActivityClass]を起動するためのIntentを指定します。
 * @param contentLayoutId 起動するFragmentが`NavHostFragment`を内包している場合、そのNavHostFragmentがセットされているview IDを指定します。
 *   [launchChildFragmentByNavController]を呼び出してFragmentを起動するときだけ意味を持ちます。
 */
abstract class FragmentScenarioPage<IMPL, F : Fragment, HA>(
        uiTestExtension: UiTestExtension<*>,
        val fragmentClass: KClass<F>,
        @Suppress("UNCHECKED_CAST")
        val hostActivityClass: KClass<HA> = FragmentTestingActivity::class as KClass<HA>,
        val hostActivityIntent: Intent = FragmentTestingActivity.createIntent(ApplicationProvider.getApplicationContext()),
        @IdRes val contentLayoutId: Int? = null
) : ActivityOrFragmentScenarioPage<IMPL, HA, F>(uiTestExtension)
        where HA : AppCompatActivity, HA : TestingFragmentHost {

    val scenario: AppCompatFragmentScenario<F, HA>
        get() = (activityOrFragmentScenario as ActivityOrFragmentScenario.FragmentScenario).scenario

    // =================
    // 画面起動系のメソッド
    // =================
    /**
     * コンストラクタ呼び出し時に指定した引数[fragmentClass]のFragmentを起動します。
     * Fragmentの起動に成功すると、
     * 対応する[AppCompatFragmentScenario] (をラップしたオブジェクト) が[activityOrFragmentScenario]に格納されます。
     *
     * @param fragmentArgs 起動するFragmentに渡す引数を指定します。
     *   [AppCompatFragmentScenario.launchInContainer]の引数にそのまま渡されます。
     * @param factory 起動するFragmentを生成するためのファクトリーを指定します。
     *   [AppCompatFragmentScenario.launchInContainer]の引数にそのまま渡されます。
     */
    fun launchFragmentSimply(fragmentArgs: Bundle? = null, factory: FragmentFactory? = null) {
        if (snapShotPageName == null) {
            snapShotPageName = fragmentClass.java.simpleName
        }
        activityOrFragmentScenario = launchFragmentWithMonitoring(fragmentArgs, factory)
    }

    /**
     * 引数で指定された生成方法でFragmentをインスタンス化して起動します。
     * 起動するFragmentは、引数[fragmentCreator]を呼び出すことによってインスタンス化します。
     *
     * @param fragmentCreator 起動したいFragmentをインスタンス化する処理が書かれたλ式を指定します。
     */
    fun launchFragmentByCreator(fragmentCreator: () -> F) {
        launchFragmentSimply(factory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return if (className == fragmentClass.java.name) {
                    fragmentCreator()
                } else {
                    super.instantiate(classLoader, className)
                }
            }
        })
    }

    /**
     * [fragmentClass]が直接内包している[SupportMapFragment]のヘルパーオブジェクトを取得します。
     * このヘルパーオブジェクトを使って地図の表示完了を待ち合わせることができます。
     *
     * [launchChildFragmentByNavController]で起動した子Fragmentに内包されている[SupportMapFragment]
     * を操作したい場合は[getSupportMapFragmentHelperOfCurrentChildFragment]を利用してください。
     *
     * @param mapFragmentId 目的の[SupportMapFragment]に対応するfragment IDを指定します。
     *   [fragmentClass]が内包している`SupportMapFragment`が1つだけの場合は指定不要です。
     */
    fun getSupportMapFragmentHelper(@IdRes mapFragmentId: Int? = null) =
            SupportMapFragmentHelper(uiTestExtension, scenario::onFragment, mapFragmentId)

    /**
     * [fragmentClass]を起動してから、[NavController]を使って特定の子Fragmentを起動します。
     * 起動に成功すると、親(ホスト)Fragmentに対応する[AppCompatFragmentScenario] (をラップしたオブジェクト) が[activityOrFragmentScenario]に格納されます。
     *
     * 起動した親Fragmentは[UiTestExtension.dataBindingIdlingResource]による監視対象になります。
     * また、目的の子Fragmentが起動するまでIdlingResourceはビジー状態になります。
     *
     * [contentLayoutId]がnullの場合は、本メソッドを呼び出すことはできません。呼び出した場合は例外が発生します。
     *
     * @param fragmentDestination 起動完了を待ちたい子Fragmentのdestination IDを指定します。
     * @param fragmentArgs 起動するFragmentに渡す引数を指定します。
     *   [AppCompatFragmentScenario.launchInContainer]の引数にそのまま渡されます。
     * @param factory 起動するFragmentを生成するためのファクトリーを指定します。
     *   [AppCompatFragmentScenario.launchInContainer]の引数にそのまま渡されます。
     * @param navigateAction 目的の子Fragmentを起動するためにNavControllerに対して何らかの操作が必要な場合、その操作内容を指定します。
     *   `null`が指定された場合は何もしません。
     */
    fun launchChildFragmentByNavController(@IdRes fragmentDestination: Int,
                                           fragmentArgs: Bundle? = null,
                                           factory: FragmentFactory? = null,
                                           navigateAction: ((NavController) -> Unit)? = null) {
        checkNotNull(contentLayoutId)
        if (snapShotPageName == null) {
            val dstName = InstrumentationRegistry.getInstrumentation().targetContext.resources.getResourceEntryName(fragmentDestination)
            snapShotPageName = dstName
        }
        uiTestExtension.countingIdlingResource.increment()
        activityOrFragmentScenario = launchFragmentWithMonitoring(fragmentArgs, factory).also { _scenario ->
            launchFragmentByNavControllerInternal(_scenario, fragmentDestination, contentLayoutId, navigateAction)
        }
    }

    /**
     * [launchChildFragmentByNavController]
     * で起動した子Fragmentを管理しているNavHostFragmentのNavControllerを引数として[block]を呼び出します。
     * [block]はメインスレッドで実行されます。
     */
    fun onNavController(block: (NavController) -> Unit) {
        onNavHostFragment { block(it.navController) }
    }

    /**
     * [launchChildFragmentByNavController]
     * で起動した子Fragmentを管理しているNavHostFragmentを引数として[block]を呼び出します。
     * [block]はメインスレッドで実行されます。
     */
    @Suppress("UNCHECKED_CAST")
    fun onNavHostFragment(block: (NavHostFragment) -> Unit) {
        checkNotNull(contentLayoutId)
        scenario.onFragment {
            val navHostFragment = it.retrieveNavHostFragment(contentLayoutId)
            block(navHostFragment)
        }
    }

    /**
     * [launchChildFragmentByNavController]を使って子Fragmentを起動している場合、
     * 本メソッド呼び出し時点で表示されている子Fragmentを引数として[block]を呼び出します。
     * [block]はメインスレッドで実行されます。
     */
    @Suppress("UNCHECKED_CAST")
    fun <CF : Fragment> onCurrentChildFragment(block: (CF) -> Unit) {
        checkNotNull(contentLayoutId)
        scenario.onFragment {
            val childFragment = it.retrieveNavHostFragment(contentLayoutId).childFragmentManager.primaryNavigationFragment as CF
            block(childFragment)
        }
    }

    /**
     * [onCurrentChildFragment]によって得られるFragmentが直接内包している[SupportMapFragment]のヘルパーオブジェクトを取得します。
     * このヘルパーオブジェクトを使って地図の表示完了を待ち合わせることができます。
     *
     * @param mapFragmentId 目的の[SupportMapFragment]に対応するfragment IDを指定します。
     *   [onCurrentChildFragment]によって得られるFragmentが内包している`SupportMapFragment`が1つだけの場合は指定不要です。
     */
    fun getSupportMapFragmentHelperOfCurrentChildFragment(@IdRes mapFragmentId: Int? = null) =
            SupportMapFragmentHelper(uiTestExtension, this::onCurrentChildFragment, mapFragmentId)

    /**
     * [fragmentClass]を起動し、[UiTestExtension.dataBindingIdlingResource]による監視対象にします。
     *
     * @param fragmentArgs 起動するFragmentに渡す引数を指定します。
     *     [FragmentScenario.launchInContainer]の引数にそのまま渡されます。
     * @param factory 起動するFragmentを生成するためのファクトリーを指定します。
     *     [FragmentScenario.launchInContainer]の引数にそのまま渡されます。
     * @return 起動したFragmentに対応する[FragmentScenario]のラッパーオブジェクトを返します。
     */
    private fun launchFragmentWithMonitoring(fragmentArgs: Bundle? = null, factory: FragmentFactory? = null) =
            AppCompatFragmentScenario.launchInContainer<F, HA>(
                    hostActivityIntent = hostActivityIntent,
                    fragmentClass = fragmentClass,
                    fragmentArgs = fragmentArgs,
                    factory = factory,
            ).let {
                uiTestExtension.dataBindingIdlingResource.monitorFragment(it)
                ActivityOrFragmentScenario.FragmentScenario(it)
            }

    // ==========================
    // スクリーンショット系のメソッド
    // ==========================

    /**
     * Viewをキャプチャします。
     * Fragmentを受け取る[func]を呼び出した結果返されるViewをキャプチャします。
     *
     * @param condition キャプチャする対象の状態(または条件)を表す文字列を指定します。
     *   結果レポートでは、このメソッドでキャプチャしたもの全てについて、この引数で指定した文字列が表示されます。
     * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
     *   `null`以外が指定された場合は結果レポートに表示されます。
     * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
     * @param func キャプチャ対象を返す関数を指定します。この関数の引数には、画面に表示されているFragmentが渡されます。
     */
    open fun captureView(condition: String, optionalDescription: String?, waitUntilIdle: Boolean, func: (F) -> View) {
        captureViewFromActivityOrFragment(condition, optionalDescription, waitUntilIdle) {
            when (it) {
                is ActivityOrFragment.Fragment -> func(it.fragment)
                is ActivityOrFragment.Activity -> throw IllegalStateException("Never reached")
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
     *   キャプチャには [FragmentScenarioPage.SequentialSnapShotTaker]が提供するメソッドが利用できます。
     */
    fun captureSequentially(condition: String, block: SequentialSnapShotTaker<F, HA>.() -> Unit) {
        val sequentialSnapShotTaker = SequentialSnapShotTaker(this, condition)
        sequentialSnapShotTaker.block()
    }

    /**
     * `(F) -> View`型の関数を指定できる [captureView] を提供する [ActivityOrFragmentSnapShotTaker.SequentialSnapShotTaker] 実装です。
     */
    class SequentialSnapShotTaker<F : Fragment, HA>(
            private val fragmentScenarioPage: FragmentScenarioPage<*, F, HA>,
            condition: String
    ) : ActivityOrFragmentSnapShotTaker.SequentialSnapShotTaker<HA, F>(fragmentScenarioPage, condition)
            where HA : AppCompatActivity, HA : TestingFragmentHost {

        /**
         * Viewをキャプチャします。
         * [FragmentScenarioPage.captureSequentially]メソッドの引数ブロックの中で利用できる[FragmentScenarioPage.captureView]です。
         *
         * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
         *   `null`以外が指定された場合は結果レポートに表示されます。
         * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
         * @param func キャプチャ対象を返す関数を指定します。この関数の引数には、画面に表示されているFragmentが渡されます。
         */
        fun captureView(optionalDescription: String?, waitUntilIdle: Boolean, func: (F) -> View) {
            fragmentScenarioPage.captureView(condition, optionalDescription, waitUntilIdle, func)
        }
    }
}