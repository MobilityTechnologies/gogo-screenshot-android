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

import android.os.SystemClock
import androidx.test.espresso.Espresso
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert

const val DEFAULT_POLL_INTERVAL = 1_000L
const val DEFAULT_TIMEOUT = 30_000L

/**
 * [condition] がtrueになるまで待ちます。[timeout] msec 経過するとタイムアウトします。
 * @return タイムアウトする前に[condition]がtrueになったときはtrueを、タイムアウトしてしまったときはfalseを、それぞれ返します。
 */
fun wait(timeout: Long = DEFAULT_TIMEOUT, interval: Long = DEFAULT_POLL_INTERVAL, condition: () -> Boolean): Boolean {
    val startTime = SystemClock.uptimeMillis()

    var result = condition()
    var elapsedTime: Long = 0
    while (!result) {
        if (elapsedTime >= timeout) {
            break
        }
        SystemClock.sleep(interval)
        result = condition()
        elapsedTime = SystemClock.uptimeMillis() - startTime
    }
    return result
}

/**
 * 引数で指定された検索条件に合致するまで待ちます。
 * タイムアウトしたときはAssertionErrorが発生します。
 *
 * @param bySelector 検索条件を指定します。
 * @param timeout タイムアウト値(msec)を指定します。デフォルトでは60秒です。
 * @param what 検索条件[bySelector]がどうなるまで待つかを指定します。[Until]クラスに定義されているメソッドを指定してください。
 *   デフォルトでは、[Until.hasObject] が指定されています。すなわち、検索条件に合致するViewが表示されるまで待ちます。
 */
fun UiDevice.waitUntil(bySelector: BySelector,
                       timeout: Long = 60_000,
                       what: (BySelector) -> SearchCondition<Boolean> = Until::hasObject) {
    // IdlingResourceがbusyの時間もタイムアウト値に含まれてしまうことを避けるため、IdlingResourceがidleになってからwaitする。
    Espresso.onIdle()
    val success = wait(what(bySelector), timeout)
    MatcherAssert.assertThat("Timed-out waiting for: ${bySelector}", success, CoreMatchers.equalTo(true))
}