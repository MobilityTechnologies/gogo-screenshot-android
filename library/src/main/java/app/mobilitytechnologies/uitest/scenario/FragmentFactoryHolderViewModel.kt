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
 * This file is a modified version of androidx.fragment.app.testing.FragmentScenario.FragmentFactoryHolderViewModel
 * included in androidx.fragment:fragment-testing:1.2.5
 *
 * Original copyright notice is as follows:
 *
 * Copyright 2018 The Android Open Source Project
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
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FragmentFactoryHolderViewModel : ViewModel() {
    companion object {
        private val FACTORY: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val viewModel = FragmentFactoryHolderViewModel()
                return viewModel as T
            }
        }

        fun getInstance(activity: AppCompatActivity): FragmentFactoryHolderViewModel {
            val viewModelProvider = ViewModelProvider(activity, FACTORY)
            return viewModelProvider.get(FragmentFactoryHolderViewModel::class.java)
        }
    }

    var fragmentFactory: FragmentFactory? = null

    override fun onCleared() {
        super.onCleared()
        fragmentFactory = null
    }
}