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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SystemUiDemoMode {

    fun display() {
        awaitSendBroadcast(
                createIntent {
                    putExtra("command", "enter")
                },
                createIntent {
                    putExtra("command", "clock")
                    putExtra("hhmm", "1000")
                },
                createIntent {
                    putExtra("command", "notifications")
                    putExtra("visible", "false")
                },
                createIntent {
                    putExtra("command", "battery")
                    putExtra("plugged", "false")
                    putExtra("level", "100")
                },
                createIntent {
                    putExtra("command", "network")
                    putExtra("wifi", "hide")
                    putExtra("mobile", "hide")
                })
    }

    fun clear() {
        val intent = createIntent {
            putExtra("command", "exit")
        }
        awaitSendBroadcast(intent)
    }

    private fun createIntent(func: Intent.() -> Unit): Intent {
        val intent = Intent("com.android.systemui.demo")
        func(intent)
        return intent
    }

    private fun awaitSendBroadcast(vararg intents: Intent) {

        val countDownLatch = CountDownLatch(intents.size)

        intents.forEach { intent ->
            ApplicationProvider.getApplicationContext<Application>().sendOrderedBroadcast(
                    intent,
                    null,
                    object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            countDownLatch.countDown()
                        }
                    },
                    null, 0, null, null)
        }

        countDownLatch.await(5, TimeUnit.SECONDS)
    }
}