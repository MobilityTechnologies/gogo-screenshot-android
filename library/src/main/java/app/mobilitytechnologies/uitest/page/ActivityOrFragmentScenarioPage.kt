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

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import app.mobilitytechnologies.uitest.espresso.waitUntilScrollBarFadedAway
import app.mobilitytechnologies.uitest.extension.UiTestExtension
import app.mobilitytechnologies.uitest.scenario.ActivityOrFragmentScenario
import app.mobilitytechnologies.uitest.snapshot.ActivityOrFragmentSnapShotTaker
import app.mobilitytechnologies.uitest.snapshot.SnapShot
import app.mobilitytechnologies.uitest.snapshot.SnapShotName
import java.util.concurrent.atomic.AtomicInteger

/**
 * [ActivityScenario]または[app.mobilitytechnologies.uitest.scenario.AppCompatFragmentScenario]を使って画面を起動するPage実装です。
 *
 * @param IMPL 実装クラスの型
 * @param A ActivityScenarioを使って起動するActivity
 * @param F AppCompatFragmentScenarioを使って起動するFragment
 */
abstract class ActivityOrFragmentScenarioPage<IMPL, A : AppCompatActivity, F : Fragment>(
        override val uiTestExtension: UiTestExtension<*>
) : Page<IMPL>, ActivityOrFragmentSnapShotTaker<A, F> {

    /**
     * 画面起動後に得られる[ActivityScenario]または[androidx.fragment.app.testing.FragmentScenario]を保持します。
     */
    var activityOrFragmentScenario: ActivityOrFragmentScenario<A, F>? = null

    /**
     * スクリーンショットを撮るときに使われる画面名です。
     */
    var snapShotPageName: String? = null

    /**
     * 同じテストメソッド内で、複数枚のスクリーンショットを撮ったときの連番です。1から始まります。
     */
    val snapShotCounter: AtomicInteger = AtomicInteger(1)
    private val snapShot: SnapShot = SnapShot()

    override fun starting() {
        snapShotCounter.set(1)
        snapShotPageName = null
        activityOrFragmentScenario = null
    }

    override fun finished() {
        activityOrFragmentScenario?.close()
    }

    // =================
    // 画面起動系のメソッド
    // =================
    protected fun launchFragmentByNavControllerInternal(targetScenario: ActivityOrFragmentScenario<A, F>,
                                                        @IdRes fragmentDestination: Int,
                                                        @IdRes viewIdOrContentLayoutId: Int,
                                                        navigateAction: ((NavController) -> Unit)? = null) {
        targetScenario.onActivityOrFragment {
            val navController = it.retrieveNavController(viewIdOrContentLayoutId)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == fragmentDestination) {
                    uiTestExtension.countingIdlingResource.decrement()
                }
            }
            navigateAction?.invoke(navController)
        }
        Espresso.onIdle()
    }

    // ==========================
    // スクリーンショット系のメソッド
    // ==========================
    override fun captureDisplay(condition: String, optionalDescription: String?, waitUntilIdle: Boolean) {
        if (waitUntilIdle) Espresso.onIdle()
        val snapShotName = SnapShotName(snapShotPageName!!, condition, snapShotCounter.getAndIncrement(), optionalDescription)
        checkNotNull(activityOrFragmentScenario).onActivity {
            snapShot.captureDisplay(snapShotName.toFileName())
        }
    }

    override fun captureActivityOrFragment(condition: String, optionalDescription: String?, waitUntilIdle: Boolean) {
        if (waitUntilIdle) Espresso.onIdle()
        val snapShotName = SnapShotName(snapShotPageName!!, condition, snapShotCounter.getAndIncrement(), optionalDescription)
        checkNotNull(activityOrFragmentScenario).onActivityOrFragment {
            when (it) {
                is ActivityOrFragment.Activity -> snapShot.capture(it.activity, snapShotName.toFileName())
                is ActivityOrFragment.Fragment -> snapShot.capture(it.fragment, snapShotName.toFileName())
            }
        }
    }

    override fun ensureInitialScrollBarFadedAway(scrollViewId: Int) {
        onView(withId(scrollViewId)).perform(waitUntilScrollBarFadedAway())
    }

    /**
     * Viewをキャプチャします。
     * 引数[func]を呼び出した結果返されるViewをキャプチャします。
     *
     * このメソッドの[func]は、引数に[ActivityOrFragment]を受け取るため、使い勝手が良くありません。
     * 各サブクラスは、このメソッドの実装を使って、
     * [func]のレシーバーがActivityまたはFragmentになるような`captureView`メソッドを定義してください。
     *
     * @param condition キャプチャ時点の画面の状態を表す文字列を指定します。結果レポートに表示されます。
     * @param optionalDescription その他、結果レポートに表示させたい補足事項があれば、それを指定します。
     *   `null`以外が指定された場合は結果レポートに表示されます。
     * @param waitUntilIdle キャプチャする前にアイドル状態になるまで待つ場合には`true`を、そうでない場合は`false`を指定します。
     * @param func キャプチャ対象を返す関数を指定します。この関数の引数には、画面に表示されているActivityまたはFragmentが渡されます。
     */
    protected fun captureViewFromActivityOrFragment(condition: String,
                                                    optionalDescription: String?,
                                                    waitUntilIdle: Boolean,
                                                    func: (ActivityOrFragment<out A, out F>) -> View) {
        if (waitUntilIdle) Espresso.onIdle()
        val snapShotName = SnapShotName(snapShotPageName!!, condition, snapShotCounter.getAndIncrement(), optionalDescription)
        checkNotNull(activityOrFragmentScenario).onActivityOrFragment {
            val view = func(it)
            when (it) {
                is ActivityOrFragment.Activity -> snapShot.capture(it.activity, view, snapShotName.toFileName())
                is ActivityOrFragment.Fragment -> snapShot.capture(it.fragment, view, snapShotName.toFileName())
            }
        }
    }
}
