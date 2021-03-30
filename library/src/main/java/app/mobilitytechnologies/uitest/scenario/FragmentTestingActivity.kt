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
 * This file is a modified version of androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity
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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Instrumented Testでフラグメントを起動したいときに利用する、ホストActivityです。
 * `FragmentScenario`が用意するホストActivityが`AppCompatActivity`を継承していないため、
 * ImageViewの`srcCompat`が無視されるなどの問題があることがわかりました。
 *
 * その問題を回避するため、`AppCompatActivity`を継承したActivityを用意しています。
 */
class FragmentTestingActivity : AppCompatActivity(), TestingFragmentHost {
    companion object {
        private const val BUNDLE_KEY_PREFIX = "app.mobilitytechnologies.uitest.scenario.FragmentTestingActivity"
        private const val THEME_EXTRAS_BUNDLE_KEY = "$BUNDLE_KEY_PREFIX.THEME_EXTRAS_BUNDLE_KEY"

        /**
         * このActivityを起動するためのIntentを返します。
         *
         * @param context
         * @param themeResId このActivityに設定するテーマのリソースIDを指定します
         */
        fun createIntent(
                context: Context,
                @StyleRes themeResId: Int = androidx.appcompat.R.style.Theme_AppCompat
        ): Intent {
            val componentName = ComponentName(context, FragmentTestingActivity::class.java)
            return Intent.makeMainActivity(componentName).putExtra(THEME_EXTRAS_BUNDLE_KEY, themeResId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = intent.getIntExtra(THEME_EXTRAS_BUNDLE_KEY, androidx.appcompat.R.style.Theme_AppCompat)
        setTheme(theme)
        FragmentFactoryHolderViewModel.getInstance(this).fragmentFactory?.let {
            supportFragmentManager.fragmentFactory = it
        }

        // androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity#onCreate
        // のコメントによると、supportFragmentManager.fragmentFactoryへのセットは、super.onCreate()よりも前に行わなければならない。
        // さもなくばActivityがクラッシュするそうだ。
        super.onCreate(savedInstanceState)
    }

    override fun replaceFragment(fragment: Fragment, tag: String?) {
        supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, fragment, tag)
                .commitNow()
    }
}