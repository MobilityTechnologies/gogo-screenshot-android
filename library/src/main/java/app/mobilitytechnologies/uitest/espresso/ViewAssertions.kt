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
 * `isBelowBottomLine()`, `findView()` and `getTopViewGroup()` are modified versions of androidx.test.espresso.assertion.PositionAssertions
 * included in androidx.test.espresso:espresso-core:3.3.0
 *
 * Original copyright notice is as follows:
 *
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.mobilitytechnologies.uitest.espresso

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.AmbiguousViewMatcherException
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import com.google.common.base.Predicate
import com.google.common.collect.Iterables
import com.google.common.collect.Iterators
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.StringDescription
import java.util.Locale

fun isBelowBottomLine(viewMatcher: Matcher<View>) = ViewAssertion { foundView, noViewException ->
    val description = StringDescription()
    if (noViewException != null) {
        description.appendText(String.format(
                Locale.ROOT,
                "' check could not be performed because view '%s' was not found.\n",
                noViewException.viewMatcherDescription))
        Log.e("TestUtils", description.toString())
        throw noViewException
    } else {
        checkNotNull(foundView)
        // TODO: describe the foundView matcher instead of the foundView itself.
        description
                .appendText("View:")
                .appendText(HumanReadables.describe(foundView))
                .appendText(" whose bottom line is not ")
                .appendText("below the bottom line of ")
                .appendText(" view ")
                .appendText(viewMatcher.toString())
        val matchedView = findView(viewMatcher, checkNotNull(getTopViewGroup(foundView)))
        val location1 = intArrayOf(0, 0)
        val location2 = intArrayOf(0, 0)
        foundView.getLocationOnScreen(location1)
        matchedView.getLocationOnScreen(location2)
        val foundViewBottom = location1[1] + foundView.height
        val matchedViewBottom = location2[1] + matchedView.height
        ViewMatchers.assertThat(
                description.toString(),
                foundViewBottom >= matchedViewBottom,
                Matchers.`is`(true))
    }
}

private fun findView(toView: Matcher<View>, root: View): View {
    val viewPredicate: Predicate<View> = Predicate { input -> toView.matches(input) }
    val matchedViewIterator: Iterator<View> = Iterables.filter(TreeIterables.breadthFirstViewTraversal(root), viewPredicate).iterator()
    var matchedView: View? = null
    while (matchedViewIterator.hasNext()) {
        matchedView = if (matchedView != null) {
            // Ambiguous!
            throw AmbiguousViewMatcherException.Builder()
                    .withRootView(root)
                    .withViewMatcher(toView)
                    .withView1(matchedView)
                    .withView2(matchedViewIterator.next())
                    .withOtherAmbiguousViews(*Iterators.toArray(matchedViewIterator, View::class.java))
                    .build()
        } else {
            matchedViewIterator.next()
        }
    }
    if (matchedView == null) {
        throw NoMatchingViewException.Builder()
                .withViewMatcher(toView)
                .withRootView(root)
                .build()
    }
    return matchedView
}

private fun getTopViewGroup(view: View): ViewGroup? {
    var currentParent = view.parent
    var topView: ViewGroup? = null
    while (currentParent != null) {
        if (currentParent is ViewGroup) {
            topView = currentParent
        }
        currentParent = currentParent.parent
    }
    return topView
}