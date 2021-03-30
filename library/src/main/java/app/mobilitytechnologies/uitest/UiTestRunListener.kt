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

import android.os.Bundle
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.RunListener

class UiTestRunListener : RunListener() {

    override fun testRunStarted(description: Description?) {
        UiTestDeveloperSettings.onTestRunStarted()
    }

    override fun testRunFinished(result: Result?) {
        UiTestDeveloperSettings.onTestRunFinished()
    }

    companion object {
        private const val RUN_LISTENER_KEY = "listener"

        fun appendListenerArgument(bundle: Bundle?): Bundle {
            val testArgumentsBundle = bundle ?: Bundle()
            val listener = testArgumentsBundle.getString(RUN_LISTENER_KEY)
            val listenerName = UiTestRunListener::class.java.name
            if (listener != null) {
                testArgumentsBundle.putString(RUN_LISTENER_KEY, listener + ",${listenerName}")
            } else {
                testArgumentsBundle.putString(RUN_LISTENER_KEY, listenerName)
            }
            return testArgumentsBundle
        }
    }
}