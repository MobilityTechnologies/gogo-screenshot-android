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

package app.mobilitytechnologies.uitest.scenario

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.mobilitytechnologies.uitest.page.ActivityOrFragment

/**
 * [androidx.test.core.app.ActivityScenario]または[AppCompatFragmentScenario]を表すラッパークラスです。
 */
sealed class ActivityOrFragmentScenario<A : AppCompatActivity, F : Fragment> {
    /** [androidx.test.core.app.ActivityScenario]を表すラッパークラスです。 */
    class ActivityScenario<A : AppCompatActivity>(val scenario: androidx.test.core.app.ActivityScenario<A>) : ActivityOrFragmentScenario<A, Nothing>()

    /** [AppCompatFragmentScenario]を表すラッパークラスです。 */
    class FragmentScenario<HA, F : Fragment>(
            val scenario: AppCompatFragmentScenario<F, HA>
    ) : ActivityOrFragmentScenario<HA, F>()
            where HA : AppCompatActivity, HA : TestingFragmentHost

    /**
     * [androidx.test.core.app.ActivityScenario.onActivity]または[AppCompatFragmentScenario.onFragment]を呼び出します。
     * [block]に渡される引数はオリジナルの`ActivityScenario`・`FragmentScenario`とは異なり、
     * `Activity`または`Fragment`どちらかのインスタンスを表す[ActivityOrFragment]クラスのインスタンスが渡されます。
     */
    fun onActivityOrFragment(block: (ActivityOrFragment<out A, out F>) -> Unit) {
        when (this) {
            is ActivityScenario -> this.scenario.onActivity {
                block(ActivityOrFragment.Activity(it))
            }
            is FragmentScenario -> this.scenario.onFragment {
                block(ActivityOrFragment.Fragment(it))
            }
        }
    }

    /**
     * [androidx.test.core.app.ActivityScenario.onActivity]または[AppCompatFragmentScenario.onActivity]を呼び出します。
     * どちらのケースでも、[block]に渡される引数にはホストしているActivityのインスタンスが渡されます。
     */
    fun onActivity(block: (A) -> Unit) {
        when (this) {
            is ActivityScenario -> this.scenario.onActivity { block(it) }
            is FragmentScenario -> this.scenario.onActivity { block(it) }
        }
    }

    /**
     * [androidx.test.core.app.ActivityScenario]または[AppCompatFragmentScenario]をクローズします。
     */
    fun close() {
        when (this) {
            is ActivityScenario -> this.scenario.close()
            is FragmentScenario -> this.scenario.close()
        }
    }
}