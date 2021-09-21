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

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import app.mobilitytechnologies.uitest.snapshot.SystemUiDemoMode
import app.mobilitytechnologies.uitest.utils.awaitExecuteShellCommand

// UIテスト用のadbや開発者optionを用いた設定をInstrumentationテストのライフサイクルにあわせて実行する
object UiTestDeveloperSettings {

    private val systemUiDemoMode = SystemUiDemoMode()

    private const val TAG : String = "${logTagPrefix} UiTestDeveloperSettings"

    // テスト実行開始の直前のタイミングに実行する
    fun onTestRunStarted() {

        val checkDumpPermissionResult = ContextCompat.checkSelfPermission(ApplicationProvider.getApplicationContext(), Manifest.permission.DUMP)

        if(checkDumpPermissionResult == PackageManager.PERMISSION_GRANTED) {
            // ステータスバーの内容を固定化するためにシステムUIデモモードを有効化する
            // https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/docs/demo_mode.md
            awaitExecuteShellCommand("settings put global sysui_demo_allowed 1")
            systemUiDemoMode.display()
        } else {
            Log.e(TAG, "Manifest.permission.DUMP is not granted. Please add uses-permission tag to your AndroidManifest.xml for testing.")
        }
    }

    // テスト実行終了の直後に実行する
    fun onTestRunFinished() {
        systemUiDemoMode.clear()
    }
}