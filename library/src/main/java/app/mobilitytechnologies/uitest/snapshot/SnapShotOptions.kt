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

import android.app.Application
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import app.mobilitytechnologies.uitest.snapshot.SnapShotOptions.Companion.DEFAULT_SETTINGS
import app.mobilitytechnologies.uitest.snapshot.SnapShotOptions.Companion.INSTRUMENTATION_ARGS_KEY_ENCODE_FILE_NAME
import java.io.File

/**
 * スクリーンショット関連のオプションを定義します。
 * デフォルト値([DEFAULT_SETTINGS]からアクセスできます)から変更したい場合は、`AndroidJUnitRunner`のサブクラスを独自に定義した上で、
 * `onStart()`メソッド内で、`super.onStart()`呼び出しより前の段階で[SnapShotOptions.currentSettings]にカスタマイズした[SnapShotOptions]をセットしてください。
 *
 * 以下に、[buildFlavorPathComponent]をデフォルト値から変更するコード例を示します。
 *
 * ```
 * class MyAndroidJUnitRunner : AndroidJUnitRunner() {
 *
 *     override fun onStart() {
 *         // super.onStart()より前に呼ぶこと
 *         SnapShotOptions.currentSettings = SnapShotOptions.DEFAULT_SETTINGS.copy(buildFlavorPathComponent = BuildConfig.FLAVOR)
 *         super.onStart()
 *         ...
 *     }
 * }
 * ```
 */
data class SnapShotOptions(
        /**
         * スクリーンショットを保存するディレクトリのルートを指定します。
         * デフォルト値は `Context#getExternalFilesDir(Environment.DIRECTORY_PICTURES)` です。
         */
        val rootDirectory: File = ApplicationProvider.getApplicationContext<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: throw IllegalStateException("getExternalFilesDir returned null. Please consider changing default SnapShotOptions#rootDirectory."),
        /**
         * スクリーンショットのファイル名のうち、日本語が指定できる部分のBase64エンコード要否を指定します。
         * trueを指定するとBase64エンコードされます。falseにするとBase64エンコードされません。
         *
         * ファイル名に日本語が含まれているとエラーになる環境がある場合に`true`を指定してください。
         *
         * デフォルトでは`adb shell am instrument`コマンドの(`-e`オプションで指定する)引数[INSTRUMENTATION_ARGS_KEY_ENCODE_FILE_NAME]の値を参照します。
         */
        val encodeFileName: Boolean = InstrumentationRegistry.getArguments().getString(INSTRUMENTATION_ARGS_KEY_ENCODE_FILE_NAME).toBoolean(),

        /**
         * スクリーンショットの撮り方を指定します。
         * 現在の実装では[ScreenshotType.ORIGINAL]または[ScreenshotType.VISUAL_REGRESSION]のいずれかを指定できます。
         * 詳細は[ScreenshotType]の説明を参照してください。
         *
         * デフォルトでは`adb shell am instrument`コマンドの(`-e`オプションで指定する)引数[ScreenshotType.KEY]の値を参照します。
         */
        val screenshotType: ScreenshotType = ScreenshotType.readInstrumentationArgument(),

        /**
         * ビルドフレーバーによって保存するディレクトリを変えたい場合に、このプロパティにフレーバー名を指定します。
         * 現状では[encodeFileName]が`false`の場合のみ意味を持ちます。
         * [encodeFileName]が`false`の場合、スクリーンショットのファイルは次のディレクトリに保存されます。
         *
         *  - このプロパティが空文字列やnullの場合
         *    `$rootDirectory/screenshots/`
         *  - このプロパティに空でない文字列が設定されている場合
         *    `$rootDirectory/screenshots/$buildFlavorPathComponent/
         *
         * このプロパティにフレーバー名を指定すると、他のビルドフレーバーで撮ったスクリーンショットが同じディレクトリに混在するのを防ぐことができます。
         */
        val buildFlavorPathComponent: String? = null,

        /**
         * スクリーンショットのファイル名の命名規則を指定します。
         * 詳しくは[SnapShotNameCreator]の説明を参照してください。
         *
         * デフォルトでは[SnapShotName.toFileName]メソッドが使われます。
         */
        val fileNameCreator: SnapShotNameCreator = SnapShotNameCreator { pageName, conditionName, counter, optionalDescription ->
            SnapShotName(pageName, conditionName, counter, optionalDescription).toFileName()
        }
) {
    companion object {
        const val INSTRUMENTATION_ARGS_KEY_ENCODE_FILE_NAME = "encodeScreenshotFileName"
        val DEFAULT_SETTINGS by lazy { SnapShotOptions() }

        private var _currentSettings: SnapShotOptions? = null
        var currentSettings: SnapShotOptions
            get() = _currentSettings ?: DEFAULT_SETTINGS
            set(value) {
                _currentSettings = value
            }
    }
}
