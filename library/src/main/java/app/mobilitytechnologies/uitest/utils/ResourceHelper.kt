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

import android.app.Application
import androidx.annotation.AnyRes
import androidx.test.core.app.ApplicationProvider

/** リソースIDを [android.content.res.Resources.getResourceName] を使って文字列に変換します。 */
fun toResourceName(@AnyRes resId: Int?): String {
    if (resId == null) return "null"
    return ApplicationProvider.getApplicationContext<Application>()
            .resources
            .getResourceName(resId)
}

/** リソースIDを [android.content.res.Resources.getResourceEntryName] を使って文字列に変換します。 */
fun toResourceEntryName(@AnyRes resId: Int?): String {
    if (resId == null) return "null"
    return ApplicationProvider.getApplicationContext<Application>()
            .resources
            .getResourceEntryName(resId)
}