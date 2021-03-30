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
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

/**
 * [FragmentActivity]または[androidx.fragment.app.Fragment]を表すラッパークラスです。
 */
sealed class ActivityOrFragment<A : FragmentActivity, F : androidx.fragment.app.Fragment> {

    /**
     * このActivityまたはFragmentがNavHostFragmentを持っているときに、それに対応するNavControllerを取得します。
     *
     * @param viewIdOrContentLayoutId NavControllerを探すために必要なview IDを指定します。
     *   `NavHostFragment`がセットされているview IDを指定してください。
     */
    abstract fun retrieveNavController(@IdRes viewIdOrContentLayoutId: Int): NavController

    class Activity<A : FragmentActivity>(val activity: A) : ActivityOrFragment<A, Nothing>() {
        override fun retrieveNavController(viewIdOrContentLayoutId: Int) = activity.findNavController(viewIdOrContentLayoutId)
    }

    class Fragment<F : androidx.fragment.app.Fragment>(val fragment: F) : ActivityOrFragment<Nothing, F>() {
        override fun retrieveNavController(viewIdOrContentLayoutId: Int) =
                fragment.retrieveNavHostFragment(viewIdOrContentLayoutId).findNavController()
    }
}