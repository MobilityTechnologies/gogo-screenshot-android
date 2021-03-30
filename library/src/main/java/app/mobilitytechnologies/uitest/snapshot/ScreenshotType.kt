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

package app.mobilitytechnologies.uitest.snapshot

import androidx.test.platform.app.InstrumentationRegistry

enum class ScreenshotType(val value: String) {
    /**
     * 画面に表示されたままの形でスクリーンショットを撮ることを表すScreenshotTypeです。
     * Visual Regression Testで利用すると微妙な差分が生じることがある一方、現実に近いスクリーンショットが撮れます。
     */
    ORIGINAL("original") {
        override fun scope(): List<ScreenshotType> {
            return listOf(this)
        }
    },

    /**
     * Visual Regression Testで、できるだけ差分が出ないようにスクリーンショットを撮ることを表すScreenshotTypeです。
     * 現在の実装では、GoogleMapで表示される地図で差分が出るのを抑えるため、地図部分を非表示にします。
     * (グレー単色で塗り潰されます)
     */
    VISUAL_REGRESSION("visual_regression") {
        override fun scope(): List<ScreenshotType> {
            return listOf(this)
        }
    },

    /**
     * [ORIGINAL]と[VISUAL_REGRESSION]それぞれのスクリーンショットを撮ることを表すScreenshotTypeです。
     * 現在の実装ではサポートしていません。
     */
    ALL("all") {
        override fun scope(): List<ScreenshotType> {
            return listOf(ORIGINAL, VISUAL_REGRESSION)
        }
    };

    abstract fun scope(): List<ScreenshotType>

    companion object {
        fun readInstrumentationArgument(): ScreenshotType {
            return from(InstrumentationRegistry.getArguments().getString(KEY))
        }

        const val KEY = "screenshotType"
        private fun from(value: String?): ScreenshotType {
            return values().firstOrNull { it.value == value } ?: ORIGINAL
        }
    }
}