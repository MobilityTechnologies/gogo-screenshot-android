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

import android.util.Base64
import android.util.Log
import app.mobilitytechnologies.uitest.logTagPrefix

data class SnapShotName(
        val pageName: String,
        val conditionName: String,
        val counter: Int,
        val optionalDescription: String? = null
) {
    companion object {
        const val TAG = "$logTagPrefix SnapShotName"
    }

    private val base64Flag = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    private val encodedConditionName = Base64.encodeToString(conditionName.toByteArray(), base64Flag)
    private val counterPostPadding = counter.toString().padStart(3, '0')
    private val encodedOptionalDescription = Base64.encodeToString(optionalDescription.orEmpty().toByteArray(), base64Flag)

    private fun toEncodedName(): String {
        val encodedResult = "$pageName!$encodedConditionName!$counterPostPadding!$encodedOptionalDescription"
        Log.d(TAG, "readable filename: ${toReadableName()}")
        Log.d(TAG, "encoded filename: $encodedResult")
        return encodedResult
    }

    /**
     * スクリーンショットを保存すべき相対パスを返します。
     * 相対パスの起点は [SnapShot.baseDir] に定義されています。
     * 現状では`$rootDirectory/screenshots/`が相対パスの起点です。
     * (`$rootDirectory`の値は[SnapShotOptions.rootDirectory]参照)
     *
     * [SnapShotOptions.encodeFileName]が`true`のときは、ファイル名に非ASCII文字が入り込まないように、エンコードしたパスを返します。
     * そのときのパス名にはディレクトリコンポーネントは含まれません。
     * ([toEncodedName]メソッドの実装を参照)
     *
     * そうでないときは、人間が読み易いように適切にサブディレクトリで分類された、日本語文字列を含むパスを返します。
     * ([toReadableName]メソッドの実装を参照)
     */
    fun toFileName() = if (SnapShotOptions.currentSettings.encodeFileName) toEncodedName() else toReadableName()

    private fun toReadableName(): String {
        val optionalDescriptionPart = optionalDescription?.let { "-$it" }.orEmpty()
        val buildFlavorPathComponent = SnapShotOptions.currentSettings.buildFlavorPathComponent
        val flavorPart = if (buildFlavorPathComponent.isNullOrEmpty()) "" else "${buildFlavorPathComponent}/"
        return "${flavorPart}${pageName}/$conditionName-${counterPostPadding}${optionalDescriptionPart}"
    }
}