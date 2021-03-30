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

package app.mobilitytechnologies.uitest.espresso

import android.view.View
import androidx.test.espresso.ViewAssertion
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * 引数 [viewAssertion] を満たす場合にマッチするようなView Matcherです。
 */
fun withViewAssertion(viewAssertion: ViewAssertion): Matcher<View> = object : TypeSafeMatcher<View>() {
    override fun matchesSafely(item: View?): Boolean = try {
        viewAssertion.check(item, null)
        true
    } catch (e: AssertionFailedError) {
        false
    }

    override fun describeTo(description: Description?) {
        // do nothing
    }
}