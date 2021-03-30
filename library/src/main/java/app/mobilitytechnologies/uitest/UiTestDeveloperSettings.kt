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

package app.mobilitytechnologies.uitest

import androidx.test.platform.app.InstrumentationRegistry
import app.mobilitytechnologies.uitest.snapshot.SystemUiDemoMode
import app.mobilitytechnologies.uitest.utils.awaitExecuteShellCommand

// UIテスト用のadbや開発者optionを用いた設定をInstrumentationテストのライフサイクルにあわせて実行する
object UiTestDeveloperSettings {

    private val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
    private val systemUiDemoMode = SystemUiDemoMode()

    // テスト実行開始の直前のタイミングに実行する
    fun onTestRunStarted() {
        // ステータスバーの内容を固定化するためにシステムUIデモモードを有効化する
        // https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/docs/demo_mode.md
        awaitExecuteShellCommand("settings put global sysui_demo_allowed 1")
        systemUiDemoMode.display()

        // Android11以上で/sdcard配下にスクリーンショット画像を保存できるようにMANAGE_EXTERNAL_STORAGE権限を付与する
        // https://developer.android.com/training/data-storage/manage-all-files#enable-manage-external-storage-for-testing
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            awaitExecuteShellCommand("appops set --uid $packageName MANAGE_EXTERNAL_STORAGE allow")
        }
    }

    // テスト実行終了の直後に実行する
    fun onTestRunFinished() {
        systemUiDemoMode.clear()
    }

    // Instrumentation$finishのあとに実行する
    fun onInstrumentationFinished() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            awaitExecuteShellCommand("appops set --uid $packageName MANAGE_EXTERNAL_STORAGE deny")
        }
    }
}