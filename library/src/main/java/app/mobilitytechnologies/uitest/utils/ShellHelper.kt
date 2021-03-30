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

package app.mobilitytechnologies.uitest.utils

import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun awaitExecuteShellCommand(command: String, timeout: Long = 10, unit: TimeUnit = TimeUnit.SECONDS) {

    val fileDescriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)

    val startTimeMills = System.currentTimeMillis() // 開始時刻(ミリ秒)
    val timeoutMillis = unit.toMillis(timeout)
    val limitTimeMills = startTimeMills + timeoutMillis // タイムアウトと判定する時刻(ミリ秒)

    val reader = ParcelFileDescriptor.AutoCloseInputStream(fileDescriptor).bufferedReader()
    reader.use {
        while (reader.readLine() != null) {
            if (limitTimeMills <= System.currentTimeMillis()) {
                throw TimeoutException()
            }
        }
    }
}