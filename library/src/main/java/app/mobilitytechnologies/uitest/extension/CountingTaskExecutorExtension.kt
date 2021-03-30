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
 * This file is a modified version of androidx.arch.core.executor.testing.CountingTaskExecutorRule
 * included in androidx.arch.core:core-testing:2.1.0
 *
 * Original copyright notice is as follows:
 *
 * Copyright (C) 2017 The Android Open Source Project
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
package app.mobilitytechnologies.uitest.extension

import android.os.SystemClock
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.DefaultTaskExecutor
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * [CountingTaskExecutorRule](https://developer.android.com/reference/androidx/arch/core/executor/testing/CountingTaskExecutorRule)
 * をJUnit 5のExtensionに移植したものです。
 *
 */
open class CountingTaskExecutorExtension : BeforeEachCallback, AfterEachCallback {

    private val countLock = Object()
    private var taskCount = 0

    override fun beforeEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(object : DefaultTaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                super.executeOnDiskIO(CountingRunnable(runnable))
            }

            override fun postToMainThread(runnable: Runnable) {
                super.postToMainThread(CountingRunnable(runnable))
            }
        })
    }

    override fun afterEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    fun increment() {
        synchronized(countLock) { taskCount++ }
    }

    fun decrement() {
        synchronized(countLock) {
            taskCount--
            if (taskCount == 0) {
                onIdle()
                countLock.notifyAll()
            }
        }
    }

    /**
     * Called when the number of awaiting tasks reaches to 0.
     *
     * @see .isIdle
     */
    protected open fun onIdle() {}

    /**
     * Returns false if there are tasks waiting to be executed, true otherwise.
     *
     * @return False if there are tasks waiting to be executed, true otherwise.
     *
     * @see .onIdle
     */
    fun isIdle(): Boolean {
        synchronized(countLock) { return taskCount == 0 }
    }

    /**
     * Waits until all active tasks are finished.
     *
     * @param time The duration to wait
     * @param timeUnit The time unit for the `time` parameter
     *
     * @throws InterruptedException If thread is interrupted while waiting
     * @throws TimeoutException If tasks cannot be drained at the given time
     */
    @kotlin.jvm.Throws(InterruptedException::class, TimeoutException::class)
    fun drainTasks(time: Int, timeUnit: TimeUnit) {
        val end = SystemClock.uptimeMillis() + timeUnit.toMillis(time.toLong())
        synchronized(countLock) {
            while (taskCount != 0) {
                val now = SystemClock.uptimeMillis()
                val remaining = end - now
                if (remaining > 0) {
                    countLock.wait(remaining)
                } else {
                    throw TimeoutException("could not drain tasks")
                }
            }
        }
    }

    inner class CountingRunnable(private val wrapped: Runnable) : Runnable {
        override fun run() {
            try {
                wrapped.run()
            } finally {
                decrement()
            }
        }

        init {
            increment()
        }
    }
}