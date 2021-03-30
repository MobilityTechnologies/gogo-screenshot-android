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
 *
 * ========
 * This file is a modified version of androidx.fragment.app.testing.FragmentScenario
 * included in androidx.fragment:fragment-testing:1.2.5
 *
 * Original copyright notice is as follows:
 *
 * Copyright (C) 2018 The Android Open Source Project
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
package app.mobilitytechnologies.uitest.scenario

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import app.mobilitytechnologies.uitest.espresso.DataBindingIdlingResource
import app.mobilitytechnologies.uitest.scenario.AppCompatFragmentScenario.Companion.launchInContainer
import kotlin.reflect.KClass

/**
 * ホストActivityにAppCompatActivityを利用した[androidx.fragment.app.testing.FragmentScenario]実装です。
 *
 * オリジナルのFragmentScenarioの実装をほぼ流用しています。オリジナルとの違いは次の通りです。
 * - [launchInContainer]の`hostActivityIntent`引数で、ホストするActivityを指定できます。ただし、ホストするActivityは次の制約を満たさなければなりません。
 *   - [AppCompatActivity]のサブタイプであること
 *   - [TestingFragmentHost]インターフェイスを実装していること
 * - UIを表示しない[androidx.fragment.app.testing.FragmentScenario.launch]メソッドは提供していません。
 * - 内部で保持している[ActivityScenario]のメソッドのうち、以下のものを公開しています。
 *   - [onActivity]
 *   - [close]
 * - 各メソッドは`this`を返しません(メソッドチェーンできません)。
 */
class AppCompatFragmentScenario<F : Fragment, HA> private constructor(
        private val fragmentClass: KClass<F>,
        private val activityScenario: ActivityScenario<HA>
) where HA : AppCompatActivity, HA : TestingFragmentHost {

    companion object {
        private const val FRAGMENT_TAG = "AppCompatFragmentScenario_Fragment_Tag"

        /**
         * 型[F]のFragmentを、型[HA]のホストActivityの中で起動します。
         * ホストActivityは、[TestingFragmentHost.replaceFragment]を使ってFragmentを起動します。
         *
         * @param F 起動したいFragmentの型を指定します
         * @param HA 起動したいFragmentをホストするActivityの型を指定します。
         *   [AppCompatActivity]のサブクラスで、かつ[TestingFragmentHost]を実装している必要があります
         * @param fragmentClass 起動したいFragmentのクラスを指定します
         * @param hostActivityIntent ホストするActivityを起動するIntentを指定します
         * @param fragmentArgs Fragmentに渡す引数を指定します
         * @param factory [FragmentFactory]を指定します
         */
        fun <F : Fragment, HA> launchInContainer(
                fragmentClass: KClass<F>,
                hostActivityIntent: Intent,
                fragmentArgs: Bundle? = null,
                factory: FragmentFactory? = null,
        ): AppCompatFragmentScenario<F, HA>
                where HA : AppCompatActivity, HA : TestingFragmentHost {
            val fragmentScenario: AppCompatFragmentScenario<F, HA> = AppCompatFragmentScenario(fragmentClass, ActivityScenario.launch(hostActivityIntent))
            fragmentScenario.activityScenario.onActivity { activity: HA ->
                factory?.let {
                    FragmentFactoryHolderViewModel.getInstance(activity).fragmentFactory = it
                    activity.supportFragmentManager.fragmentFactory = it
                }
                val classLoader = fragmentClass.java.classLoader!!
                val fragment = activity.supportFragmentManager.fragmentFactory.instantiate(classLoader, fragmentClass.java.name)
                if (fragmentArgs != null) {
                    fragment.arguments = fragmentArgs
                }
                activity.replaceFragment(fragment, FRAGMENT_TAG)
            }
            return fragmentScenario
        }
    }

    fun onActivity(action: (HA) -> Unit) = activityScenario.onActivity(action)

    /**
     * Runs a given [action] on the current Activity's main thread.
     *
     * Note that you should never keep Fragment reference passed into your [action]
     * because it can be recreated at anytime during state transitions.
     *
     * Throwing an exception from [action] makes the host Activity crash. You can
     * inspect the exception in logcat outputs.
     *
     * This method cannot be called from the main thread.
     */
    @Suppress("UNCHECKED_CAST")
    fun onFragment(action: (F) -> Unit) {
        activityScenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
            checkNotNull(fragment) {
                "The fragment has been removed from FragmentManager already."
            }
            check(fragmentClass.isInstance(fragment))
            action(fragment as F)
        }
    }

    /**
     * Moves Fragment state to a new state.
     *
     *  If a new state and current state are the same, this method does nothing. It accepts
     * [CREATED][State.CREATED], [STARTED][State.STARTED], [RESUMED][State.RESUMED],
     * and [DESTROYED][State.DESTROYED]. [DESTROYED][State.DESTROYED] is a terminal state.
     * You cannot move to any other state after the Fragment reaches that state.
     *
     *  This method cannot be called from the main thread.
     *
     * *Note: Moving state to [STARTED][State.STARTED] is not supported on Android API
     * level 23 and lower. [UnsupportedOperationException] will be thrown.*
     */
    fun moveToState(newState: Lifecycle.State) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && newState == Lifecycle.State.STARTED) {
            throw UnsupportedOperationException(
                    "Moving state to STARTED is not supported on Android API level 23 and lower."
                            + " This restriction comes from the combination of the Android framework bug"
                            + " around the timing of onSaveInstanceState invocation and its workaround code"
                            + " in FragmentActivity. See http://issuetracker.google.com/65665621#comment3"
                            + " for more information.")
        }
        if (newState == Lifecycle.State.DESTROYED) {
            activityScenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                // Null means the fragment has been destroyed already.
                if (fragment != null) {
                    activity.supportFragmentManager
                            .beginTransaction()
                            .remove(fragment)
                            .commitNowAllowingStateLoss()
                }
            }
        } else {
            activityScenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                checkNotNull(fragment) { "The fragment has been removed from FragmentManager already." }
            }
            activityScenario.moveToState(newState)
        }
    }

    /**
     * Recreates the host Activity.
     *
     * After this method call, it is ensured that the Fragment state goes back to the same state
     * as its previous state.
     *
     * This method cannot be called from the main thread.
     */
    fun recreate() {
        activityScenario.recreate()
    }

    /**
     * 内部で保持している、ホストActivityの[ActivityScenario]をクローズします。
     */
    fun close() = activityScenario.close()
}

/**
 * Sets the fragment from a [AppCompatFragmentScenario] to be used from [DataBindingIdlingResource].
 */
fun DataBindingIdlingResource.monitorFragment(fragmentScenario: AppCompatFragmentScenario<out Fragment, *>) {
    fragmentScenario.onFragment {
        this.activity = it.requireActivity()
    }
}
